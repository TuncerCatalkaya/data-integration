import { format } from "date-fns"

export default function FormatDate(date: Date) {
    return format(date, "dd.MM.yyyy HH:mm")
}
