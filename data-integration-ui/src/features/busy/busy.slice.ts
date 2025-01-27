import { createSlice, PayloadAction } from "@reduxjs/toolkit"

interface BusyState {
    busyCounter: number
    isBusy: boolean
    texts: string[]
}

const initialState: BusyState = {
    busyCounter: 0,
    isBusy: false,
    texts: []
}

const BusySlice = createSlice({
    name: "busySlice",
    initialState,
    reducers: {
        setBusy: (state, action: PayloadAction<string | undefined>) => {
            state.busyCounter += 1
            state.isBusy = true
            if (action.payload) {
                state.texts = state.texts.concat(action.payload)
            }
        },
        setIdle: (state, action: PayloadAction<string | undefined>) => {
            state.busyCounter -= 1
            state.isBusy = state.busyCounter > 0
            if (action.payload) {
                const firstIndex = state.texts.indexOf(action.payload)
                state.texts = state.texts.filter((_, index) => index !== firstIndex)
            }
        }
    }
})

export default BusySlice
