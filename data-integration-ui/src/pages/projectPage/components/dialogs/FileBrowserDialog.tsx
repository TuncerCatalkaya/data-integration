import {
    Alert,
    AlertColor,
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    Paper,
    PaperProps,
    Stack,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import {useSnackbar} from "notistack"
import theme from "../../../../theme"
import {useTranslation} from "react-i18next"
import {CloudDownload, CloudUpload, Delete, Storage} from "@mui/icons-material"
import {ChangeEvent, useCallback, useEffect, useState} from "react"
import DataIntegrationSpinner from "../../../../components/./dataIntegrationSpinner/DataIntegrationSpinner"
import FormatDate from "../../../../utils/FormatDate"
import {filesize} from "filesize"
import useConfirmationDialog from "../../../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../../../components/confirmationDialog/ConfirmationDialog"
import {S3Api} from "../../../../features/s3/s3.api"
import {CompletedPart, S3ListResponse} from "../../../../features/s3/s3.types"
import pLimit from "p-limit"
import GetFrontendEnvironment from "../../../../utils/GetFrontendEnvironment"
import GenerateScopeKey from "../../../../utils/GenerateScopeKey"
import ImportDataDialog from "../../../projectImportPage/components/importDataDialog/ImportDataDialog"
import IsUnsupportedFileType from "../../../../utils/IsUnsupportedFileType";

interface FileBrowserDialogProps {
    open: boolean
    handleClickClose: () => void
    projectId: string
    handleClickStartImportS3: (key: string) => Promise<void>
}

interface AlertType {
    text: string
    severity?: AlertColor
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#file-browser-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

const MB = 1024 * 1024

export default function FileBrowserDialog({ open, handleClickClose, projectId, handleClickStartImportS3 }: Readonly<FileBrowserDialogProps>) {
    const [uploadProgress, setUploadProgress] = useState<number>(0)
    const [isUploading, setIsUploading] = useState<boolean>(false)
    const [fileBrowserObjects, setFileBrowserObjects] = useState<S3ListResponse[]>([])
    const [alert, setAlert] = useState<AlertType>({ text: "" })
    const [fileBrowserObjectToDelete, setFileBrowserObjectToDelete] = useState<string>("")
    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()
    const [openImportDataDialog, setOpenImportDataDialog] = useState(false)

    const [initiateMultipartUpload] = S3Api.useInitiateMultipartUploadMutation()
    const [generatePresignedUrlMultiPartUpload] = S3Api.useGeneratePresignedUrlMultiPartUploadMutation()
    const [completeMultipartUpload] = S3Api.useCompleteMultipartUploadMutation()
    const [abortMultipartUpload] = S3Api.useAbortMultipartUploadMutation()
    const [listObjectsV2] = S3Api.useListObjectsV2Mutation()
    const [deleteObject] = S3Api.useDeleteObjectMutation()

    const { enqueueSnackbar } = useSnackbar()
    const translation = useTranslation()

    const handleCloseAlert = () => setAlert({ text: "" })

    const handleClickOpenImportDataDialog = () => setOpenImportDataDialog(true)
    const handleClickCloseImportDataDialog = () => setOpenImportDataDialog(false)

    const handleFileChange = async (e: ChangeEvent<HTMLInputElement>, delimiter: string) => {
        if (delimiter === "select") {
            enqueueSnackbar("Please select a delimiter.", { variant: "error" })
        } else {
            const files = e.target.files
            if (files) {
                const file = files[0]
                e.target.value = ""
                if (IsUnsupportedFileType(file)) {
                    enqueueSnackbar("Please select a CSV file", { variant: "error" })
                } else if (isUploading) {
                    enqueueSnackbar("Another file is already uploading.", { variant: "warning" })
                } else {
                    const bucket = GetFrontendEnvironment("VITE_S3_BUCKET")
                    const scopeKey = GenerateScopeKey(file)
                    const key = projectId + "/" + scopeKey

                    const CHUNK_SIZE = GetFrontendEnvironment("VITE_S3_CHUNK_SIZE_IN_MB") * MB
                    const CONCURRENCY = +GetFrontendEnvironment("VITE_S3_CONCURRENCY")

                    let uploadId: string | undefined
                    try {
                        setIsUploading(true)

                        const initiateMultipartUploadResponse = await initiateMultipartUpload({ bucket, key }).unwrap()

                        uploadId = initiateMultipartUploadResponse.uploadId
                        const totalParts = Math.ceil(file.size / CHUNK_SIZE)

                        const uploadProgress = (1 / totalParts) * 100

                        const promises: Promise<CompletedPart>[] = []

                        let totalLineCount = 0
                        const limit = pLimit(CONCURRENCY)
                        for (let partNumber = 1; partNumber <= totalParts; partNumber++) {
                            const start = (partNumber - 1) * CHUNK_SIZE
                            const end = Math.min(start + CHUNK_SIZE, file.size)
                            const filePart = file.slice(start, end)

                            const uploadPromise = limit(() =>
                                uploadFilePart(filePart, bucket, key, uploadId!, partNumber, uploadProgress, lineCount => (totalLineCount += lineCount))
                            )

                            promises.push(uploadPromise)
                        }

                        const uploadedParts = await Promise.all(promises)

                        const completeMultipartUploadResponse = await completeMultipartUpload({
                            bucket,
                            key,
                            uploadId,
                            lineCount: totalLineCount - 1,
                            delimiter,
                            completedParts: uploadedParts
                        })
                        setIsUploading(false)
                        setUploadProgress(0)
                        if (completeMultipartUploadResponse.error) {
                            setAlert({ text: "Could not upload file", severity: "error" })
                            setIsUploading(false)
                            setUploadProgress(0)
                        } else {
                            setAlert({ text: "File is uploaded", severity: "success" })
                            await fetchFileBrowserObjects()
                        }
                    } catch (e) {
                        setAlert({ text: "Something went wrong during the upload", severity: "error" })
                        setIsUploading(false)
                        setUploadProgress(0)
                        if (uploadId) {
                            await abortMultipartUpload({ bucket, key, uploadId })
                        }
                        console.error(e)
                    }
                }
            }
        }
        e.target.value = ""
    }

    const uploadFilePart = async (
        filePart: Blob,
        bucket: string,
        key: string,
        uploadId: string,
        partNumber: number,
        uploadProgress: number,
        updateTotalLineCount: (lineCount: number) => void
    ): Promise<CompletedPart> => {
        try {
            const lineCount = await countLinesInBlob(filePart)
            updateTotalLineCount(lineCount)

            const generatePresignedUrlMultiPartUploadResponse = await generatePresignedUrlMultiPartUpload({ bucket, key, uploadId, partNumber }).unwrap()

            const response = await fetch(generatePresignedUrlMultiPartUploadResponse.presignedUrl, {
                method: "PUT",
                body: filePart
            })

            if (!response.ok) {
                return Promise.reject(new Error(`Failed to upload part ${partNumber}.`))
            }

            const etag = response.headers.get("Etag")
            if (!etag) {
                return Promise.reject(new Error("Missing ETag in the response."))
            }

            setUploadProgress(prevState => prevState + uploadProgress)

            return { etag, partNumber }
        } catch (error) {
            console.error(error)
            return Promise.reject(new Error("Something went wrong during file part upload."))
        }
    }

    function countLinesInBlob(blob: Blob): Promise<number> {
        return new Promise((resolve, reject) => {
            const reader = new FileReader()
            reader.readAsText(blob)

            reader.onload = () => {
                const content = reader.result as string
                const lines = content.split(/\r?\n|\r/).filter(line => line.trim() !== "")
                resolve(lines.length)
            }

            reader.onerror = err => {
                reject(err)
            }
        })
    }

    const handleClickDeleteFileBrowserObject = async (key: string) => {
        const bucket = GetFrontendEnvironment("VITE_S3_BUCKET")
        const deleteObjectResponse = await deleteObject({ bucket, key })
        if (deleteObjectResponse.error) {
            setAlert({ text: "Could not delete file", severity: "error" })
        } else {
            setAlert({ text: "File deleted", severity: "success" })
            setFileBrowserObjectToDelete("")
            await fetchFileBrowserObjects()
        }
    }

    const fetchFileBrowserObjects = useCallback(async () => {
        const bucket = GetFrontendEnvironment("VITE_S3_BUCKET")
        const listObjectsV2Response = await listObjectsV2({ bucket, projectId })
        if (listObjectsV2Response.data) {
            setFileBrowserObjects(listObjectsV2Response.data)
        } else if (listObjectsV2Response.error) {
            setAlert({ text: "Could not retrieve files", severity: "error" })
        }
    }, [listObjectsV2, projectId])

    useEffect(() => {
        fetchFileBrowserObjects()
    }, [fetchFileBrowserObjects])

    return (
        <>
            {isUploading && <DataIntegrationSpinner determinate={{ text: "Uploading: ", value: uploadProgress }} />}
            {openImportDataDialog && (
                <ImportDataDialog open={openImportDataDialog} handleClickClose={handleClickCloseImportDataDialog} handleFileChange={handleFileChange} />
            )}
            {openConfirmationDialog && (
                <ConfirmationDialog
                    open={openConfirmationDialog}
                    handleClickClose={handleClickCloseConfirmationDialog}
                    handleClickYes={() => handleClickDeleteFileBrowserObject(fileBrowserObjectToDelete)}
                >
                    <Stack spacing={2}>
                        <Typography variant="h6">Are you sure you want to delete the file?</Typography>
                        <Typography sx={{ fontWeight: "bold" }}>{fileBrowserObjectToDelete}</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Dialog
                open={open}
                onClose={handleClickClose}
                aria-labelledby="file-browser-dialog"
                PaperComponent={PaperComponent}
                sx={{ zIndex: theme.zIndex.modal }}
            >
                <DialogTitle sx={{ cursor: "move" }}>
                    <Stack spacing={1}>
                        <Stack direction="row" display="flex" justifyContent="space-between">
                            <Stack direction="row" alignItems="center" spacing={1}>
                                <Storage />
                                <Typography variant="h6">{translation.t("pages.project.components.dialogs.fileBrowserDialog.title")}</Typography>
                            </Stack>
                            <Button
                                color="error"
                                variant="contained"
                                startIcon={<CloudUpload />}
                                onClick={handleClickOpenImportDataDialog}
                                sx={{ marginRight: 2, backgroundColor: "#C72E49" }}
                            >
                                {"Upload"}
                            </Button>
                        </Stack>
                        {alert.text && (
                            <Alert severity={alert.severity} onClose={handleCloseAlert}>
                                {alert.text}
                            </Alert>
                        )}
                    </Stack>
                </DialogTitle>
                <DialogContent>
                    <Stack spacing={1} padding={2}>
                        {fileBrowserObjects.length === 0 && <Typography variant="h6">No files uploaded yet for this project</Typography>}
                        {[...fileBrowserObjects]
                            .sort((a, b) => new Date(b.lastModified).getTime() - new Date(a.lastModified).getTime())
                            .map(fileBrowserObject =>
                                    <Paper key={fileBrowserObject.key} elevation={5} sx={{ minWidth: 500, padding: 2, borderRadius: 5 }}>
                                        <Stack spacing={1}>
                                            <Typography
                                                noWrap
                                                sx={{
                                                    maxWidth: 500,
                                                    textOverflow: "ellipsis",
                                                    overflow: "hidden",
                                                    whiteSpace: "nowrap",
                                                    fontWeight: "bold"
                                                }}
                                            >
                                                {fileBrowserObject.key.split("/")[1]}
                                            </Typography>
                                            <Typography>{filesize(fileBrowserObject.size, { standard: "iec" })}</Typography>
                                            <Stack direction="row" display="flex" justifyContent="space-between" alignItems="center" spacing={1}>
                                                <Typography sx={{ fontStyle: "italic" }}>{FormatDate(fileBrowserObject.lastModified)}</Typography>
                                                <Stack direction="row" spacing={1}>
                                                    <Button
                                                        color="error"
                                                        variant="contained"
                                                        endIcon={<Delete />}
                                                        onClick={() => {
                                                            setFileBrowserObjectToDelete(fileBrowserObject.key)
                                                            handleClickOpenConfirmationDialog()
                                                        }}
                                                    >
                                                        Delete
                                                    </Button>
                                                    {!fileBrowserObject.checkpoint && (
                                                        <Button
                                                            variant="contained"
                                                            endIcon={<CloudDownload />}
                                                            onClick={async () => await handleClickStartImportS3(fileBrowserObject.key)}
                                                        >
                                                            {"Start Import"}
                                                        </Button>
                                                    )}
                                                    {fileBrowserObject.checkpoint && (
                                                        <Button
                                                            variant="contained"
                                                            color="warning"
                                                            endIcon={<CloudDownload />}
                                                            onClick={async () => await handleClickStartImportS3(fileBrowserObject.key)}
                                                            sx={{ color: theme.palette.common.white }}
                                                        >
                                                            {"Restart Import"}
                                                        </Button>
                                                    )}
                                                </Stack>
                                            </Stack>
                                        </Stack>
                                    </Paper>
                            )}
                    </Stack>
                </DialogContent>
            </Dialog>
        </>
    )
}
