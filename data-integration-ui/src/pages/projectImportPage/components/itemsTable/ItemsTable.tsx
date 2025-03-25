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
                    sortable: false
                },
                ...[...headers].map(key => ({
                    colId: key.name,
                    headerName: key.display,
                    headerTooltip: key.name,
                    field: `properties.${key.name}.value`,
                    cellRenderer: UndoCellRenderer,
                    cellRendererParams: (params: ValueGetterParams) => ({
                        value: params.data.properties[key.name]?.value,
                        originalValue: params.data.properties[key.name]?.originalValue,
                        onUndo: () => {
                            updateItemProperty({
                                projectId: projectId!,
                                itemId: params.data.id,
                                key: key.name,
                                newValue: params.data.properties[key.name]?.originalValue
                            }).then(response => {
                                if (response.data) {
                                    const newData = {
                                        ...params.data,
                                        properties: {
                                            ...params.data.properties,
                                            [key.name]: {
                                                ...params.data.properties[key.name],
                                                value: response.data.properties[key.name].value,
                                                originalValue: response.data.properties[key.name].originalValue
                                            }
                                        }
                                    }
                                    params.api.applyTransaction({ update: [newData] })
                                }
                            })
                        }
                    }),
                    tooltipValueGetter: (params: ITooltipParams) => {
                        const originalValue = params.data?.properties?.[key.name]?.originalValue
                        if (originalValue) {
                            return "original: " + params.data?.properties?.[key.name]?.originalValue ?? ""
                        }
                    },
                    valueSetter: (params: ValueSetterParams) => {
                        updateItemProperty({ projectId: projectId!, itemId: params.data.id, key: key.name, newValue: params.newValue ?? "" }).then(response => {
                            if (response.data) {
                                const newData = {
                                    ...params.data,
                                    properties: {
                                        ...params.data.properties,
                                        [key.name]: {
                                            ...params.data.properties[key.name],
                                            value: response.data.properties[key.name].value,
                                            originalValue: response.data.properties[key.name].originalValue
                                        }
                                    }
                                }
                                params.api.applyTransaction({ update: [newData] })
                            }
                        })
                        return true
                    },
                    cellStyle: (params: CellClassParams) => {
                        const originalValue: string | undefined = params.data?.properties?.[key.name]?.originalValue
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
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    tooltipShowDelay={1000}
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
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
