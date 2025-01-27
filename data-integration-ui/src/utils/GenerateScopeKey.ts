import TimeStamp from "./TimeStamp"

export default function GenerateScopeKey(file: File) {
    const timeStamp = TimeStamp()
    const splittedFileName = file.name.split(".")
    const fileName = splittedFileName[0]
    const extension = splittedFileName[1]
    return fileName + "-" + timeStamp + "." + extension
}
