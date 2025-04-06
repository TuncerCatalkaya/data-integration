import { styled, Tab, Tabs } from "@mui/material"
import { useEffect, useMemo, useState } from "react"
import { generatePath, useLocation, useParams } from "react-router-dom"
import ProjectTabsStyles from "./ProjectTabs.styles"
import useNavigate from "../../../../router/hooks/useNavigate"
import RouterPaths from "../../../../router/constants/RouterPaths"
import theme from "../../../../theme"

const CustomTabs = styled(Tabs)(() => ({
    "& .indicator": {
        top: "0px",
        bottom: "auto",
        height: "4px",
        display: "flex",
        justifyContent: "center",
        backgroundColor: "transparent",
        "& > span": {
            maxWidth: "50%",
            width: "100%",
            backgroundColor: theme.palette.primary.main
        }
    }
}))

const CustomTab = styled(Tab)(({ theme }) => ({
    textTransform: "none",
    backgroundColor: theme.palette.grey["500"],
    borderRadius: "8px 8px 0 0",
    marginRight: "5px",
    "&:hover": {
        color: theme.palette.primary.light
    }
}))

export default function ProjectTabs() {
    const { projectId } = useParams()

    const { toProjectDetails, toProjectImport, toProjectMappedItems } = useNavigate()
    const location = useLocation()

    const allowedTabs = useMemo(
        () => [
            generatePath(RouterPaths.PROJECT_DETAILS, { projectId }),
            generatePath(RouterPaths.PROJECT_IMPORT, { projectId }),
            generatePath(RouterPaths.PROJECT_MAPPING, { projectId })
        ],
        [projectId]
    )
    const [tab, setTab] = useState<string>(allowedTabs[0])

    const handleClickToProjectDetailsTab = () => toProjectDetails(projectId!)
    const handleClickToProjectImportTab = () => toProjectImport(projectId!)
    const handleClickToProjectMappedItemsTab = () => toProjectMappedItems(projectId!)

    useEffect(() => {
        if (allowedTabs.includes(location.pathname)) {
            setTab(location.pathname)
        }
    }, [location, allowedTabs])

    return (
        <CustomTabs
            value={tab}
            variant="scrollable"
            scrollButtons="auto"
            TabIndicatorProps={{ children: <span /> }}
            classes={{
                flexContainer: "flexContainer",
                indicator: "indicator"
            }}
            sx={ProjectTabsStyles.tabs}
        >
            <CustomTab value={allowedTabs[0]} label={"Details"} onClick={handleClickToProjectDetailsTab} disableRipple />
            <CustomTab value={allowedTabs[1]} label={"Import"} onClick={handleClickToProjectImportTab} disableRipple />
            <CustomTab value={allowedTabs[2]} label={"Mapping"} onClick={handleClickToProjectMappedItemsTab} disableRipple />
        </CustomTabs>
    )
}
