import { FixedSizeList, FixedSizeListProps } from "react-window"
import { ReactNode } from "react"

interface VirtualizedListProps<T> {
    fixedSizeListProps: Omit<FixedSizeListProps, "children">
    items: T[]
    children: (item: T, index?: number) => ReactNode
}

export default function VirtualizedList<T>({ fixedSizeListProps, items, children }: Readonly<VirtualizedListProps<T>>) {
    return <FixedSizeList {...fixedSizeListProps}>{({ index, style }) => <div style={style}>{children(items[index], index)}</div>}</FixedSizeList>
}
