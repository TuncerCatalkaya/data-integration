import React, { ChangeEvent, useState } from "react"
import { SortChangedEvent } from "ag-grid-community"

export default function usePagination() {
    const [page, setPage] = useState(0)
    const [pageSize, setPageSize] = useState(10)
    const [sort, setSort] = useState<string | undefined>(undefined)
    const [totalElements, setTotalElements] = useState(0)

    const onPageChangeHandler = (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => setPage(page)

    const onPageSizeChangeHandler = (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => {
        const newPageSize = +e.target.value
        const newPage = Math.floor((page * pageSize) / newPageSize)
        setPage(newPage)
        setPageSize(newPageSize)
    }

    const onSortChangeHandler = (e: SortChangedEvent) => {
        const sort = e.api.getColumnState().find(s => s.sort != null)
        setSort(sort ? sort.colId + "," + sort.sort : undefined)
    }

    return {
        page,
        pageSize,
        sort,
        totalElements,
        setTotalElements,
        onPageChangeHandler,
        onPageSizeChangeHandler,
        onSortChangeHandler
    }
}
