import { useAppSelector } from "../../store/store"
import TokenForm from "../../components/tokenform/TokenForm"
import { Outlet } from "react-router-dom"
import ErrorContent from "../../components/errorContent/ErrorContent"
import DataIntegrationSpinner from "../../components/./dataIntegrationSpinner/DataIntegrationSpinner"
import { useTranslation } from "react-i18next"

export default function RootPage() {
    const token = useAppSelector(state => state.auth.token)
    const isBusy = useAppSelector<boolean>(state => state.busy.isBusy)

    const translation = useTranslation()

    const invalidTokenElement = import.meta.env.DEV ? (
        <TokenForm />
    ) : (
        <ErrorContent h2={translation.t("pages.root.error.h2")} h5={translation.t("pages.root.error.h5")} />
    )

    return (
        <>
            {isBusy && <DataIntegrationSpinner />}
            {token ? <Outlet /> : invalidTokenElement}
        </>
    )
}
