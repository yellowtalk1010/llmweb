import {Fragment, useState, useEffect } from "react"

function AiCheckTemplate({ issue }) {
    console.info("ai checker: ")
    console.info(issue)

    return (
        <>
            <div>
                <a class="ai_check">AI审计</a>
                <div id={"ai_check_" + issue.id}>
                    <textarea placeholder="请输入..." rows="4" class="textarea"></textarea>
                </div>
            </div>

            <style>
                {`
                textarea {
                    width: 900px;
                    height: 100px;
                }
                `}
            </style>
        </>
    );
}


export default AiCheckTemplate;