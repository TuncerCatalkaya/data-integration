import { ScopeHeaderResponse } from "../features/projects/projects.types"

export default function GetScopeHeaders(scopeHeaders: ScopeHeaderResponse[]) {
    return scopeHeaders.filter(header => !header.hidden)
}
