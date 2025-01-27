import { TablePagination } from "@mui/material"
import React, { ChangeEvent } from "react"
import { useTranslation } from "react-i18next"

interface PaginationProps {
    page: number
    pageSize: number
    totalElements: number
    onPageChangeHandler: (_: React.MouseEvent<HTMLButtonElement> | null, page: number) => void
    onPageSizeChangeHandler: (e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => void
}

export default function Pagination({ page, pageSize, totalElements, onPageChangeHandler, onPageSizeChangeHandler }: Readonly<PaginationProps>) {
    const translation = useTranslation()
    return (
        <TablePagination
            count={totalElements}
            page={page}
            rowsPerPage={pageSize}
            rowsPerPageOptions={[10, 25, 50, 100, 250, 500, 750, 1000]}
            showFirstButton
            showLastButton
            onPageChange={onPageChangeHandler}
            onRowsPerPageChange={onPageSizeChangeHandler}
            labelRowsPerPage={translation.t("components.pagination.rowsPerPage")}
            labelDisplayedRows={({ from, to, count }) => `${from}–${to} ${translation.t("components.pagination.fromTo")} ${count}`}
            getItemAriaLabel={type => {
                if (type === "first") {
                    return translation.t("components.pagination.first")
                } else if (type === "previous") {
                    return translation.t("components.pagination.previous")
                } else if (type === "next") {
                    return translation.t("components.pagination.next")
                } else if (type === "last") {
                    return translation.t("components.pagination.last")
                }
                return ""
            }}
            sx={{ borderBottom: "none" }}
        />
    )
}
