import { Box, Button, Stack, TextField, Typography } from "@mui/material"
import { useParams } from "react-router-dom"
import { useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { ProjectResponse } from "../../features/projects/projects.types"
import theme from "../../theme"
import { Edit } from "@mui/icons-material"
import FormatDate from "../../utils/FormatDate"
import { useTranslation } from "react-i18next"

const excludedKeys = ["createdBy"]
const readOnlyKeys = ["id", "createdDate", "lastModifiedDate"]
const dateKeys = ["createdDate", "lastModifiedDate"]

export default function ProjectDetailsPage() {
    const { projectId } = useParams()
    const [projectResponse, setProjectResponse] = useState<Partial<ProjectResponse>>({})
    const [changed, setChanged] = useState<boolean>(false)

    const [getProject] = ProjectsApi.useLazyGetProjectQuery()
    const [updateProject] = ProjectsApi.useUpdateProjectMutation()

    const translation = useTranslation()

    const fetchData = useCallback(async () => {
        const response = await getProject({ projectId: projectId! })
        if (response.data) {
            setProjectResponse(response.data)
        }
    }, [getProject, projectId])

    useEffect(() => {
        fetchData()
    }, [fetchData])

    const handleInputChange = (field: keyof ProjectResponse, value: string) => {
        setChanged(true)
        setProjectResponse(prevData => ({
            ...prevData,
            [field]: value
        }))
    }

    const handleUpdateProjectClick = async () => {
        await updateProject({ projectId: projectResponse.id!, projectName: projectResponse.name! })
        setChanged(false)
        await fetchData()
    }

    return (
        <Stack width="100vw">
            <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                <Button disabled={!changed} variant="contained" onClick={handleUpdateProjectClick} endIcon={<Edit />}>
                    Save Changes
                </Button>
            </Box>
            <Stack spacing={2}>
                {Object.entries(projectResponse)
                    .filter(([key]) => !excludedKeys.includes(key))
                    .map(([key, value]) => {
                        let formattedValue = value as string
                        if (dateKeys.includes(key)) {
                            formattedValue = FormatDate(value as Date)
                        }
                        return (
                            <Stack key={key}>
                                <Typography variant="h6" align="left">
                                    {translation.t(`pages.projectDetails.fields.${key}`)}
                                </Typography>
                                <TextField
                                    disabled={readOnlyKeys.includes(key)}
                                    value={formattedValue}
                                    onChange={e => handleInputChange(key as keyof ProjectResponse, e.target.value)}
                                    sx={{ backgroundColor: theme.palette.common.white }}
                                />
                            </Stack>
                        )
                    })}
            </Stack>
        </Stack>
    )
}
