import { useState } from "react"
import { keyframes } from "@mui/material"
import { SxProps } from "@mui/system/styleFunctionSx"

const shakeAnimation = keyframes`
    0% { transform: translate(0, 0); }
    20% { transform: translate(-5px, -3px); }
    40% { transform: translate(5px, 3px); }
    60% { transform: translate(-5px, -3px); }
    80% { transform: translate(5px, 3px); }
    100% { transform: translate(0, 0); }
`

export default function useShake() {
    const [shake, setShake] = useState(false)

    const handleShakeClick = () => {
        setShake(true)
        setTimeout(() => setShake(false), 500)
    }

    const shakeSx: SxProps = {
        animation: shake ? `${shakeAnimation} 0.5s ease` : "none"
    }

    return {
        handleShakeClick,
        shakeSx
    }
}
