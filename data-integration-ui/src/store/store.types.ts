import { store } from "./store"

export type RootState = ReturnType<typeof store.getState>
export type Dispatch = typeof store.dispatch

export interface DataIntegrationExtraOptions {
    busyText?: string
    skipBusy?: boolean
}
