import {Fragment, useState, useEffect } from "react"


function RunTemplate() {

    return (
        <>
            <form>
                <div>
                <div>
                    <button>运行</button>
                </div>
                <div>
                    <span>命令:</span>
                </div>
                <input type='text'></input>
                </div>
                <div>
                <div>
                    <span>参数:</span>
                </div>
                <textarea></textarea>
                </div>
            </form>
            <style>
                {`
                    form {
                        font-family: Arial, sans-serif;
                        max-width: 800px;
                        margin: 20px auto;
                        padding: 20px;
                        border: 1px solid #ddd;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    
                    form > div {
                        margin-bottom: 20px;
                    }
                    
                    input[type='text'] {
                        width: 100%;
                        padding: 10px;
                        margin-top: 5px;
                        border: 1px solid #ccc;
                        border-radius: 4px;
                        box-sizing: border-box;
                        font-size: 14px;
                    }
                    
                    textarea {
                        width: 100%;
                        height: 300px; /* Approximately 100 lines */
                        min-height: 300px;
                        padding: 10px;
                        margin-top: 5px;
                        border: 1px solid #ccc;
                        border-radius: 4px;
                        box-sizing: border-box;
                        font-family: monospace;
                        font-size: 14px;
                        resize: both;
                        overflow: auto; /* Enables scrollbars when needed */
                    }
                    
                    button {
                        background-color: #4CAF50;
                        color: white;
                        border: none;
                        padding: 10px 20px;
                        text-align: center;
                        text-decoration: none;
                        display: inline-block;
                        font-size: 16px;
                        margin: 4px 2px;
                        cursor: pointer;
                        border-radius: 4px;
                        transition: background-color 0.3s;
                    }
                    
                    button:hover {
                        background-color: #45a049;
                    }
                    
                    span {
                        font-weight: bold;
                        color: #333;
                    }
                    `}
            </style>
        </>
    );
}

export default RunTemplate;