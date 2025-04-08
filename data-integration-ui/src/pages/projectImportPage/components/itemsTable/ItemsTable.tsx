import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { Stack } from "@mui/material"
import { ItemResponse, ScopeHeaderResponse } from "../../../../features/projects/projects.types"
import {
    CellClassParams,
    CheckboxSelectionCallbackParams,
    ColDef,
    GetRowIdParams,
    GridApi,
    GridReadyEvent,
    IRowNode,
    ITooltipParams,
    SelectionChangedEvent,
    SortChangedEvent,
    ValueGetterParams
} from "ag-grid-community"
import "./ItemsTable.css"
import React, { ChangeEvent, Dispatch, SetStateAction, useCallback, useEffect, useRef } from "react"
import Pagination from "../../../../components/pagination/Pagination"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { ValueSetterParams } from "ag-grid-community/dist/types/core/entities/colDef"
import CheckboxTableHeader from "../../../../components/checkboxTableHeader/CheckboxTableHeader"
import UndoCellRenderer from "../../../../components/undoCellRenderer/UndoCellRenderer"
import GetScopeHeaders from "../../../../utils/GetScopeHeaders"

interface ItemsTableProps {
    rowData: ItemResponse[]
    scopeHeaders: ScopeHeaderResponse[]
    columnDefs: ColDef[]
    setColumnDefs: Dispatch<SetStateAction<ColDef[]>>
    setSelectedItems: Dispatch<SetStateAction<string[]>>
    mapping: string
    page: number
    pageSize: number
    totalElements: number
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

export default function ItemsTable({
    rowData,
    scopeHeaders,
    columnDefs,
    setColumnDefs,
    setSelectedItems,
    mapping,
    ...itemsTableProps
}: Readonly<ItemsTableProps>) {
    const { projectId } = useParams()

    const [updateItemProperty] = ProjectsApi.useUpdateItemPropertyMutation()

    const onCheck = useCallback(
        (node: IRowNode) => {
            return mapping !== "select" && !node.data.mappingIds.includes(mapping)
        },
        [mapping]
    )

    useEffect(() => {
        const headers = GetScopeHeaders(scopeHeaders)
        if (rowData.length > 0 && headers.length > 0) {
            const dynamicColumnDefs: ColDef[] = [
                {
                    colId: "checkboxSelection",
                    headerName: "",
                    field: "checkboxSelection",
                    maxWidth: 50,
                    resizable: false,
                    headerComponent: rowData && mapping !== "select" && CheckboxTableHeader,
                    headerComponentParams: {
                        mapping,
                        rowData,
                        onCheck
                    },
                    checkboxSelection: (params: CheckboxSelectionCallbackParams) => {
                        return mapping !== "select" && !params.data.mappingIds.includes(mapping)
                    },
                    lockPosition: true,
                    filter: false,
                    editable: false,
                    sortable: false,
                    pinned: "left"
                },
                ...[...headers].map(key => ({
                    colId: "dynamic_" + key.id,
                    headerName: key.display,
                    headerTooltip: key.display,
                    cellRenderer: UndoCellRenderer,
                    cellRendererParams: (params: ValueGetterParams) => ({
                        value: params.data.properties[key.id]?.value,
                        originalValue: params.data.properties[key.id]?.originalValue,
                        onUndo: () => {
                            updateItemProperty({
                                projectId: projectId!,
                                itemId: params.data.id,
                                key: key.id,
                                newValue: params.data.properties[key.id]?.originalValue
                            }).then(response => {
                                if (response.data) {
                                    const newData = {
                                        ...params.data,
                                        properties: {
                                            ...params.data.properties,
                                            [key.id]: {
                                                ...params.data.properties[key.id],
                                                value: response.data.properties[key.id].value,
                                                originalValue: response.data.properties[key.id].originalValue
                                            }
                                        }
                                    }
                                    params.api.applyTransaction({ update: [newData] })
                                }
                            })
                        }
                    }),
                    tooltipValueGetter: (params: ITooltipParams) => {
                        const originalValue = params.data?.properties?.[key.id]?.originalValue
                        if (originalValue) {
                            return "original: " + params.data?.properties?.[key.id]?.originalValue
                        }
                    },
                    valueGetter: (params: ValueGetterParams) => {
                        return params.data?.properties?.[key.id]?.value || ""
                    },
                    valueSetter: (params: ValueSetterParams) => {
                        updateItemProperty({ projectId: projectId!, itemId: params.data.id, key: key.id, newValue: params.newValue ?? "" }).then(response => {
                            if (response.data) {
                                const newData = {
                                    ...params.data,
                                    properties: {
                                        ...params.data.properties,
                                        [key.id]: {
                                            ...params.data.properties[key.id],
                                            value: response.data.properties[key.id].value,
                                            originalValue: response.data.properties[key.id].originalValue
                                        }
                                    }
                                }
                                params.api.applyTransaction({ update: [newData] })
                            }
                        })
                        return true
                    },
                    cellStyle: (params: CellClassParams) => {
                        const originalValue: string | undefined = params.data?.properties?.[key.id]?.originalValue
                        const edited = originalValue !== undefined && originalValue !== null
                        if (edited) {
                            return { background: "#fff3cd", zIndex: -1 }
                        } else {
                            return { background: "inherit", zIndex: -1 }
                        }
                    }
                }))
            ]
            setColumnDefs(dynamicColumnDefs)
        } else {
            setColumnDefs([])
        }
    }, [rowData, setColumnDefs, scopeHeaders, projectId, updateItemProperty, mapping, onCheck, setSelectedItems])

    const defaultColDef: ColDef = {
        filter: true,
        editable: true
    }

    const getRowId = (params: GetRowIdParams) => params.data.id

    const onSelectionChanged = useCallback(
        (e: SelectionChangedEvent) => {
            const selectedNodes = e.api.getSelectedNodes()
            const itemIds = selectedNodes.map(node => node.id!)
            setSelectedItems(itemIds)
        },
        [setSelectedItems]
    )

    const gridApiRef = useRef<GridApi | null>(null)

    useEffect(() => {
        if (gridApiRef.current && columnDefs.length > 0) {
            setTimeout(() => {
                gridApiRef.current?.autoSizeAllColumns()
            }, 1)
        }
    }, [columnDefs.length])

    const onGridReady = (event: GridReadyEvent) => (gridApiRef.current = event.api)

    return (
        <Stack>
            <div className="ag-theme-alpine" style={{ height: 488, textAlign: "left" }}>
                <AgGridReact
                    rowData={columnDefs.length === 0 ? [] : rowData}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    tooltipShowDelay={1000}
                    tooltipHideDelay={60000}
                    tooltipInteraction
                    enableCellTextSelection
                    stopEditingWhenCellsLoseFocus
                    getRowId={getRowId}
                    rowSelection="multiple"
                    suppressRowHoverHighlight
                    suppressRowClickSelection
                    suppressDragLeaveHidesColumns
                    suppressColumnMoveAnimation
                    suppressMovableColumns
                    onSelectionChanged={onSelectionChanged}
                    onGridReady={onGridReady}
                    localeText={{
                        noRowsToShow:
                            rowData.length > 0 && columnDefs.length === 0
                                ? "No columns available to display, are they all hidden? Navigate to the edit header dialog."
                                : "No rows to show"
                    }}
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
