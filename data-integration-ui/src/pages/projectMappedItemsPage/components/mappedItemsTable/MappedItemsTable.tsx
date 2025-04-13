import "ag-grid-community/styles/ag-grid.css"
import "ag-grid-community/styles/ag-theme-alpine.css"
import { AgGridReact } from "ag-grid-react"
import { Stack } from "@mui/material"
import { ItemStatusResponse, MappedItemResponse, MappingResponse, ScopeHeaderResponse, ScopeResponse } from "../../../../features/projects/projects.types"
import {
    CellClassParams,
    CheckboxSelectionCallbackParams,
    ColDef,
    EditableCallbackParams,
    GetRowIdParams,
    GridApi,
    GridReadyEvent,
    ICellRendererParams,
    IRowNode,
    SelectionChangedEvent,
    SortChangedEvent,
    ValueGetterParams
} from "ag-grid-community"
import "./MappedItemsTable.css"
import React, { ChangeEvent, Dispatch, MutableRefObject, SetStateAction, useCallback, useEffect, useMemo } from "react"
import Pagination from "../../../../components/pagination/Pagination"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { ValueSetterParams } from "ag-grid-community/dist/types/core/entities/colDef"
import CheckboxTableHeader from "../../../../components/checkboxTableHeader/CheckboxTableHeader"
import UndoCellRenderer from "../../../../components/undoCellRenderer/UndoCellRenderer"
import { DataIntegrationHeaderAPIResponse } from "../../../../features/hosts/hosts.types"
import ColorTooltip from "../../../../components/tooltip/ColorTooltip"
import CellStatusIcon from "../../../../components/cellStatus/CellStatusIcon"

interface ItemsTableProps {
    gridApiRef: MutableRefObject<GridApi | null>
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

const NUMBER_OF_ROWS_WITHOUT_DYNAMIC_DATA = 2

export default function MappedItemsTable({
    gridApiRef,
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
                    sortable: false,
                    pinned: "left"
                },
                {
                    colId: "status",
                    headerName: "Status",
                    maxWidth: 200,
                    resizable: false,
                    lockPosition: true,
                    filter: false,
                    editable: false,
                    sortable: false,
                    pinned: "left",
                    cellRenderer: CellStatusIcon,
                    cellRendererParams: (params: ICellRendererParams) => ({
                        status: params.data.status,
                        errorMessages: params.data.errorMessages
                    })
                },
                ...[...getHostHeadersResponse.headers]
                    .filter(target => selectedMapping.mapping[target.id])
                    .flatMap(target =>
                        selectedMapping.mapping[target.id].map(sourceKey => ({
                            colId: "dynamic_" + target.id,
                            headerName: target.display,
                            headerTooltip: target.tooltip,
                            headerClass: target.optional ? "optional-header" : undefined,
                            tooltipComponent: ColorTooltip,
                            tooltipComponentParams: {
                                color: "info",
                                messages: [target.tooltip],
                                maxHeight: 175
                            },
                            editable: (params: EditableCallbackParams) => params.data.status !== ItemStatusResponse.INTEGRATED,
                            cellRenderer: UndoCellRenderer,
                            cellRendererParams: (params: ValueGetterParams) => ({
                                value: getValue(params.data, sourceKey, target.id),
                                freeze: params.data.status === ItemStatusResponse.INTEGRATED,
                                originalValue: params.data.properties?.[target.id]?.originalValue,
                                onUndo: () => {
                                    updateMappedItemProperty({
                                        projectId: projectId!,
                                        mappedItemId: params.data.id,
                                        key: target.id
                                    }).then(response => {
                                        if (response.data) {
                                            const revertedData = {
                                                ...params.data,
                                                properties: {
                                                    ...params.data.properties,
                                                    [target.id]: undefined
                                                }
                                            }
                                            params.api.applyTransaction({ update: [revertedData] })
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
                                    if (response.data) {
                                        const newData = {
                                            ...params.data,
                                            properties: {
                                                ...params.data.properties,
                                                [target.id]: {
                                                    ...(params.data.properties ? params.data.properties[target.id] : {}),
                                                    value: response.data.properties[target.id].value,
                                                    originalValue: response.data.properties[target.id].originalValue
                                                }
                                            }
                                        }
                                        params.api.applyTransaction({ update: [newData] })
                                    }
                                })
                                return true
                            },
                            cellStyle: (params: CellClassParams) => {
                                if (params.data.status === ItemStatusResponse.INTEGRATED) {
                                    return { background: "#ddefdd", zIndex: -1 }
                                }
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
        getHostHeadersResponse.headers,
        gridApiRef
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
            e.api.clearFocusedCell()
        },
        [setSelectedItems]
    )

    useEffect(() => {
        if (gridApiRef.current && columnDefs.length > 0) {
            setTimeout(() => {
                gridApiRef.current?.autoSizeAllColumns()
            }, 1)
        }
    }, [gridApiRef, columnDefs.length])

    const onGridReady = (event: GridReadyEvent) => (gridApiRef.current = event.api)

    const noRowsMessage = useMemo(() => {
        if (selectedScope && selectedMapping && columnDefs.length === 2) {
            return "No columns available to display, are they not mapped? Edit the mapping and make sure that the targets are mapped to one source."
        } else if (selectedScope && selectedMapping) {
            return "No rows to show"
        } else if (selectedScope) {
            return "Select a mapping to show data"
        } else {
            return "Select a scope"
        }
    }, [selectedScope, selectedMapping, columnDefs.length])

    const gridKey = `${selectedScope?.id}-${selectedMapping?.id}-${columnDefs.length}`

    return (
        <Stack>
            <div className="ag-theme-alpine" style={{ height: 488, textAlign: "left" }}>
                <AgGridReact
                    key={gridKey}
                    rowData={selectedScope && selectedMapping && columnDefs.length === NUMBER_OF_ROWS_WITHOUT_DYNAMIC_DATA ? [] : rowData}
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
                    localeText={{ noRowsToShow: noRowsMessage }}
                />
            </div>
            <Pagination {...itemsTableProps} />
        </Stack>
    )
}
