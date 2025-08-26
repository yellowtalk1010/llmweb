import {Fragment, useState, useEffect } from "react"

function AiCheckTemplate({ issue }) {
    console.info("ai checker: " + issue)

    return (
        <>
            <div>
                <a class="ai_check" id={"ai_check_" + issue.id}>AI审计</a>
            </div>
        </>
    );
}


export default AiCheckTemplate;