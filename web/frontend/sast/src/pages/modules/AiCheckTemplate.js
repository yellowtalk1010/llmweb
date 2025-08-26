import {Fragment, useState, useEffect } from "react"

function AiCheckTemplate({ issue }) {
    console.info("ai checker: ")
    console.info(issue)

    return (
        <>
            <div>
                <a class="ai_check">AI审计</a>
                <div class="ai_float">
                    <textarea placeholder="请输入..." rows="4" class="textarea"></textarea>
                    <br></br>
                    <button class="check-btn">审计</button> 
                    <button class="close-btn">关闭</button>
                    <br></br>
                    <textarea class="textarea_hidden" id="textarea_hidden-${issueId}" hidden="hidden"></textarea>
                    <div class="result" id={"ai_check_" + issue.id}></div>
                </div>

            </div>

            <style>
                {`
                textarea {
                    width: 900px;
                    height: 100px;
                }
                    
                .close-btn {
                    margin-top: 10px;
                    padding: 5px 10px;
                    background-color: #f44336;
                    color: #fff;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                }

                .check-btn {
                    margin-top: 10px;
                    padding: 5px 10px;
                    background-color: green;
                    color: #fff;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                }
                `}
            </style>
        </>
    );
}


export default AiCheckTemplate;