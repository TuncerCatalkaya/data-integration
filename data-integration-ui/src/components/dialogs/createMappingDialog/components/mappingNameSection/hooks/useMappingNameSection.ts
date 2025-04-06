import { useRef, useState } from "react"

export default function useMappingNameSection() {
    const mappingNameRef = useRef<HTMLInputElement | undefined>()
    const [mappingName, setMappingName] = useState("")
    const [mappingNameError, setMappingNameError] = useState(false)

    const handleMappingNameChange = () => setMappingNameError(false)

    return {
        mappingNameRef,
        mappingName,
        setMappingName,
        mappingNameError,
        setMappingNameError,
        handleMappingNameChange
    }
}
