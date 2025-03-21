import { ChangeEvent, useState } from "react"

export default function useMappingNameSection() {
    const [mappingNameKey, setMappingNameKey] = useState("")
    const [mappingName, setMappingName] = useState("")

    const handleMappingNameChange = (event: ChangeEvent<HTMLInputElement>) => {
        const newMappingName = event.target.value
        setMappingName(newMappingName)
    }

    return {
        mappingNameKey,
        setMappingNameKey,
        mappingName,
        setMappingName,
        handleMappingNameChange
    }
}
