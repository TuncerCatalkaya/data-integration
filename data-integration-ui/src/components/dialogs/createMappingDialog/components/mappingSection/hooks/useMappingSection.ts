import { useCallback, useEffect, useState } from "react"
import { MappingsInput } from "../types/MappingSection.types"
import { v4 as uuidv4 } from "uuid"

export default function useMappingSection() {
    const [mappings, setMappings] = useState<MappingsInput[]>([])
    const [selectedMapping, setSelectedMapping] = useState<MappingsInput>({
        id: "",
        header: "",
        values: []
    })
}
