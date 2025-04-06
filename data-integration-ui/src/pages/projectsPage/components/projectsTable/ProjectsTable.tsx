import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { IconButton, ListItemIcon, Menu, MenuItem, Stack, Typography } from "@mui/material"
import "./ProjectsTable.css"
import useNavigate from "../../../../router/hooks/useNavigate"
import { ColDef, ICellRendererParams, SortChangedEvent } from "ag-grid-community"
import { Delete, Launch, MoreVert } from "@mui/icons-material"
import React, { ChangeEvent, useState } from "react"
import FormatDate from "../../../../utils/FormatDate"
import Pagination from "../../../../components/pagination/Pagination"
import { useTranslation } from "react-i18next"
import { ProjectResponse } from "../../../../features/projects/projects.types"
import useConfirmationDialog from "../../../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../../../components/confirmationDialog/ConfirmationDialog"

interface ProjectsTableProps {
    rowData: ProjectResponse[]
    handleClickDeleteProject: (projectId: string) => void
    page: number
    pageSize: number
    totalElements: number
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

enum ProjectAction {
    OPEN,
    DELETE
}

export default function ProjectsTable(projectsTableProps: Readonly<ProjectsTableProps>) {
    const navigate = useNavigate()
    const translation = useTranslation()

    const { openConfirmationDialog, handleClickOpenConfirmationDialog, handleClickCloseConfirmationDialog } = useConfirmationDialog()

    const [projectId, setProjectId] = useState("")
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null)

    const handleMenuOpen = (e: React.MouseEvent<HTMLElement>, projectId: string) => {
        setAnchorEl(e.currentTarget)
        setProjectId(projectId)
    }

    const handleMenuClose = () => {
        setAnchorEl(null)
    }

    const handleActionClick = (projectAction: ProjectAction) => {
        if (projectAction === ProjectAction.OPEN) {
            navigate.toProject(projectId)
        } else if (projectAction === ProjectAction.DELETE) {
            handleClickOpenConfirmationDialog()
        }
        handleMenuClose()
    }

    const actionElement = (params: ICellRendererParams) => (
        <IconButton onClick={e => handleMenuOpen(e, params.data.id)}>
            <MoreVert />
        </IconButton>
    )

    const [columnDefs] = useState<ColDef[]>([
        { headerName: translation.t("pages.projects.components.projectsTable.columns.id"), field: "id", tooltipField: "id", maxWidth: 200 },
        { headerName: translation.t("pages.projects.components.projectsTable.columns.projectName"), field: "name", tooltipField: "name" },
        {
            headerName: translation.t("pages.projects.components.projectsTable.columns.createdDate"),
            field: "createdDate",
            maxWidth: 150,
            valueFormatter: params => FormatDate(params.value)
        },
        {
            headerName: translation.t("pages.projects.components.projectsTable.columns.lastModifiedDate"),
            field: "lastModifiedDate",
            maxWidth: 150,
            valueFormatter: params => FormatDate(params.value)
        },
        {
            headerName: "",
            field: "action",
            maxWidth: 80,
            sortable: false,
            cellRenderer: actionElement
        }
    ])

    const defaultColDef: ColDef = {
        flex: 1,
        resizable: false,
        comparator: () => 0
    }

    const getRowStyle = () => {
        return {
            cursor: "pointer"
        }
    }

    return (
        <Stack>
            {openConfirmationDialog && (
                <ConfirmationDialog
                    open={openConfirmationDialog}
                    handleClickClose={handleClickCloseConfirmationDialog}
                    handleClickYes={() => projectsTableProps.handleClickDeleteProject(projectId)}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">{"Are you sure you want to delete the project?"}</Typography>
                        <Stack>
                            <Typography variant="body1">{"ID of project:"}</Typography>
                            <Typography variant="body1" fontWeight={"bold"}>
                                {projectId}
                            </Typography>
                        </Stack>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
                <MenuItem onClick={() => handleActionClick(ProjectAction.OPEN)}>
                    <ListItemIcon>
                        <Launch fontSize="small" />
                    </ListItemIcon>
                    {"Open"}
                </MenuItem>
                <MenuItem onClick={() => handleActionClick(ProjectAction.DELETE)}>
                    <ListItemIcon>
                        <Delete fontSize="small" />
                    </ListItemIcon>
                    {"Delete"}
                </MenuItem>
            </Menu>
            <div className="ag-theme-alpine" style={{ width: 900, height: 471, textAlign: "left" }}>
                <AgGridReact
                    rowData={projectsTableProps.rowData}
                    columnDefs={columnDefs}
                    onSortChanged={projectsTableProps.onSortChangeHandler}
                    defaultColDef={defaultColDef}
                    tooltipShowDelay={1000}
                    tooltipInteraction
                    suppressMovableColumns
                    suppressCellFocus
                    suppressColumnMoveAnimation
                    animateRows={false}
                    suppressAnimationFrame
                    getRowStyle={getRowStyle}
                    onCellClicked={e => {
                        if (e.colDef.field === "action") {
                            return
                        }
                        navigate.toProject(e.data.id)
                    }}
                />
            </div>
            <Pagination {...projectsTableProps} />
        </Stack>
    )
}
