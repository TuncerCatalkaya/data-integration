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
    GridApi,
    GridReadyEvent,
    IRowNode,
    SelectionChangedEvent,
    SortChangedEvent,
    ValueGetterParams
} from "ag-grid-community"
import "./MappedItemsTable.css"
import React, { ChangeEvent, Dispatch, SetStateAction, useCallback, useEffect, useRef } from "react"
import Pagination from "../../../../components/pagination/Pagination"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { ValueSetterParams } from "ag-grid-community/dist/types/core/entities/colDef"
import CheckboxTableHeader from "../../../../components/checkboxTableHeader/CheckboxTableHeader"
import UndoCellRenderer from "../../../../components/undoCellRenderer/UndoCellRenderer"
import { DataIntegrationHeaderAPIResponse } from "../../../../features/hosts/hosts.types"

interface ItemsTableProps {
    rowData: MappedItemResponse[]
    scopeHeaders: ScopeHeaderResponse[]
    selectedScope?: ScopeResponse
    selectedMapping?: MappingResponse
    getHostHeadersResponse: DataIntegrationHeaderAPIResponse
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
    getHostHeadersResponse,
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
    const getValue = (singleRowData: any, sourceKey: string, targetKey: string) => {
        if (singleRowData.properties?.[targetKey] != null) {
            return singleRowData.properties[targetKey].value
        }
        return singleRowData.item.properties[sourceKey]?.value
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
                ...[...getHostHeadersResponse.headers]
                    .filter(target => selectedMapping.mapping[target.id])
                    .flatMap(target =>
                        selectedMapping.mapping[target.id].map(sourceKey => ({
                            colId: target.id,
                            headerName: target.display,
                            headerTooltip: target.tooltip,
                            cellRenderer: UndoCellRenderer,
                            cellRendererParams: (params: ValueGetterParams) => ({
                                value: getValue(params.data, sourceKey, target.id),
                                originalValue: params.data.properties?.[target.id]?.originalValue,
                                onUndo: () => {
                                    updateMappedItemProperty({
                                        projectId: projectId!,
                                        mappedItemId: params.data.id,
                                        key: target.id
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
                            valueGetter: (params: ValueGetterParams) => getValue(params.data, sourceKey, target.id),
                            valueSetter: (params: ValueSetterParams) => {
                                updateMappedItemProperty({
                                    projectId: projectId!,
                                    mappedItemId: params.data.id,
                                    key: target.id,
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
                                const originalValue: string | undefined = params.data.properties[target.id]?.originalValue
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
        selectedScope,
        getHostHeadersResponse.headers
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
                    rowData={selectedScope && selectedMapping && columnDefs.length === 1 ? [] : rowData}
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
                    localeText={{
                        noRowsToShow:
                            selectedScope && selectedMapping && columnDefs.length === 1
                                ? "No columns available to display, are they not mapped? Edit the mapping and make sure that the targets are mapped to one source."
                                : "No rows to show"
                    }}
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
