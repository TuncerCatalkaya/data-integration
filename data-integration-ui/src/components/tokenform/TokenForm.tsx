import { Button, FormHelperText, Stack, TextareaAutosize } from "@mui/material"
import { ChangeEvent, useState } from "react"
import { useAppDispatch } from "../../store/store"
import AuthSlice from "../../features/auth/auth.slice"
import { AuthUtils } from "../../features/auth/auth.utils"
import { useTranslation } from "react-i18next"
import theme from "../../theme"

export default function TokenForm() {
    const [token, setToken] = useState<string>("")
    const [helperText, setHelperText] = useState<string>("")
    const dispatch = useAppDispatch()

    const translation = useTranslation()

    const handleChangeToken = (e: ChangeEvent<HTMLTextAreaElement>) => {
        setToken(e.target.value)
        setHelperText("")
    }
    const handleClickToken = () => {
        if (token === "") {
            setHelperText(translation.t("components.tokenForm.helperText.emptyToken"))
            return
        }
        if (!AuthUtils.isJwtTokenValid(token)) {
            setHelperText(translation.t("components.tokenForm.helperText.invalidToken"))
            return
        }
        dispatch(AuthSlice.actions.setToken(token))
        setHelperText("")
    }

    return (
        <Stack alignItems="flex-end" spacing={1}>
            <TextareaAutosize
                placeholder={translation.t("components.tokenForm.placeholder")}
                minRows={3}
                maxRows={20}
                spellCheck={false}
                style={{
                    width: "500px",
                    resize: "none",
                    borderColor: helperText !== "" ? theme.palette.error.main : theme.palette.text.secondary
                }}
                onChange={handleChangeToken}
            />
            <FormHelperText error={helperText !== ""}>{helperText}</FormHelperText>
            <Button variant="contained" onClick={handleClickToken}>
                {translation.t("components.tokenForm.submitButton")}
            </Button>
        </Stack>
    )
}
