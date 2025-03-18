import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { Stack } from "@mui/material"
import { ItemStatusResponse, MappedItemResponse, MappingResponse, ScopeHeaderResponse, ScopeResponse } from "../../../../features/projects/projects.types"
import {
    CellClassParams,
    CheckboxSelectionCallbackParams,
    ColDef,
    GetRowIdParams,
    IRowNode,
    SelectionChangedEvent,
    SortChangedEvent,
    ValueGetterParams
} from "ag-grid-community"
import "./MappedItemsTable.css"
import React, { ChangeEvent, Dispatch, SetStateAction, useCallback, useEffect } from "react"
import Pagination from "../../../../components/pagination/Pagination"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { ValueSetterParams } from "ag-grid-community/dist/types/core/entities/colDef"
import CheckboxTableHeader from "../../../../components/checkboxTableHeader/CheckboxTableHeader"
import UndoCellRenderer from "../../../../components/undoCellRenderer/UndoCellRenderer"
import GetScopeHeaders from "../../../../utils/GetScopeHeaders"

interface ItemsTableProps {
    rowData: MappedItemResponse[]
    scopeHeaders: ScopeHeaderResponse[]
    selectedScope?: ScopeResponse
    selectedMapping?: MappingResponse
    columnDefs: ColDef[]
    setColumnDefs: Dispatch<SetStateAction<ColDef[]>>
    setSelectedItems: Dispatch<SetStateAction<string[]>>
    mapping: string
    fetchMappedItemsData: (mappingId: string, scopeId: string, page: number, pageSize: number, sort?: string) => Promise<void>
    page: number
    pageSize: number
    totalElements: number
    sort?: string
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
    onSortChangeHandler: (e: SortChangedEvent) => void
}

export default function MappedItemsTable({
    rowData,
    scopeHeaders,
    selectedScope,
    selectedMapping,
    columnDefs,
    setColumnDefs,
    setSelectedItems,
    mapping,
    fetchMappedItemsData,
    ...itemsTableProps
}: Readonly<ItemsTableProps>) {
    const { projectId } = useParams()

    const [updateMappedItemProperty] = ProjectsApi.useUpdateMappedItemPropertyMutation()

    /* eslint-disable @typescript-eslint/no-explicit-any */
    const getValue = (singleRowData: any, key: string, mappedKey: string) => {
        if (singleRowData.properties?.[mappedKey] != null) {
            return singleRowData.properties[mappedKey].value
        }
        return singleRowData.item.properties[key]?.value
    }

    const onCheck = useCallback(
        (node: IRowNode) => {
            return mapping !== "select" && node.data.status !== ItemStatusResponse.INTEGRATED
        },
        [mapping]
    )

    useEffect(() => {
        if (selectedScope && selectedMapping && rowData.length > 0) {
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
                        return mapping !== "select" && params.data.status !== ItemStatusResponse.INTEGRATED
                    },
                    lockPosition: true,
                    filter: false,
                    editable: false,
                    sortable: false
                },
                ...[...GetScopeHeaders(scopeHeaders)].flatMap(key =>
                    selectedMapping.mapping[key].map(mappedKey => ({
                        colId: mappedKey,
                        headerName: mappedKey,
                        cellRenderer: UndoCellRenderer,
                        cellRendererParams: (params: ValueGetterParams) => ({
                            value: getValue(params.data, key, mappedKey),
                            originalValue: params.data.properties?.[mappedKey]?.originalValue,
                            onUndo: (originalValue: string) => {
                                updateMappedItemProperty({
                                    projectId: projectId!,
                                    mappedItemId: params.data.id,
                                    key: mappedKey,
                                    newValue: originalValue
                                }).then(response => {
                                    if (response) {
                                        fetchMappedItemsData(
                                            selectedScope.id,
                                            selectedMapping.id,
                                            itemsTableProps.page,
                                            itemsTableProps.pageSize,
                                            itemsTableProps.sort
                                        )
                                    }
                                })
                            }
                        }),
                        valueGetter: (params: ValueGetterParams) => getValue(params.data, key, mappedKey),
                        valueSetter: (params: ValueSetterParams) => {
                            updateMappedItemProperty({
                                projectId: projectId!,
                                mappedItemId: params.data.id,
                                key: mappedKey,
                                newValue: params.newValue ?? ""
                            }).then(response => {
                                if (response) {
                                    fetchMappedItemsData(
                                        selectedScope.id,
                                        selectedMapping.id,
                                        itemsTableProps.page,
                                        itemsTableProps.pageSize,
                                        itemsTableProps.sort
                                    )
                                }
                            })
                            return true
                        },
                        cellStyle: (params: CellClassParams) => {
                            if (params.data.properties == null) {
                                return { background: "inherit", zIndex: -1 }
                            }
                            const originalValue: string | undefined = params.data.properties[mappedKey]?.originalValue
                            const edited = originalValue !== undefined && originalValue !== null
                            if (edited) {
                                return { background: "#fff3cd", zIndex: -1 }
                            } else {
                                return { background: "inherit", zIndex: -1 }
                            }
                        }
                    }))
                )
            ]
            setColumnDefs(dynamicColumnDefs)
        }
    }, [
        rowData,
        setColumnDefs,
        scopeHeaders,
        projectId,
        updateMappedItemProperty,
        fetchMappedItemsData,
        itemsTableProps.page,
        itemsTableProps.pageSize,
        itemsTableProps.sort,
        mapping,
        onCheck,
        selectedMapping,
        selectedScope
    ])

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
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
