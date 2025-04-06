import { format } from "date-fns"

export default function TimeStamp() {
    return format(new Date(), "ddMMyyyyHHmmss")
}
