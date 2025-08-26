import { useState } from "react";

function AiCheckTemplate({ issue }) {
    console.info("ai checker: ");
    console.info(issue);
    
    const [isOpen, setIsOpen] = useState(false);
    const [aiConnect, setAiConnect] = useState(true); //判断ai是否连接成功
    
    const handleAiCheckClick = () => {
        setIsOpen(true);
    };
    
    const handleCloseClick = () => {
        setIsOpen(false);
    };

    var websocketResult = null;
    // 创建 WebSocket 连接（仅一个连接复用）
    const socket = new WebSocket("ws://localhost:8080/ws");

    socket.addEventListener('open', () => {
        console.log('✅ WebSocket 连接已打开');
    });

    socket.addEventListener('error', (err) => {
        console.error('❌ WebSocket 连接出错', err);
    });

    socket.onmessage = function (event) {
        console.log("收到消息: " + event.data);
        console.log(websocketResult)

        // //将数据写入到隐藏输入框中
        // document.getElementById("textarea_hidden-" + websocketResult).value
        //     = document.getElementById("textarea_hidden-" + websocketResult).value + event.data

        // const input = document.getElementById("textarea_hidden-" + websocketResult).value;
        // const html = marked.parse(input);
        // console.info("html:" + html)
        // document.getElementById("result-" + websocketResult).innerHTML = html;
    };

    return (
        <>
            <div style={{ position: 'relative', display: 'inline-block' }}>
                <a 
                    className="ai_check" 
                    onClick={handleAiCheckClick}
                    style={{ 
                        cursor: 'pointer',
                        color: '#1976d2',
                        textDecoration: 'none'
                    }}
                >
                    AI审计
                </a>
                
                {isOpen && (
                    <div 
                        className="ai_float"
                        style={{
                            position: 'absolute',
                            top: '100%',
                            left: 0,
                            marginTop: '5px',
                            zIndex: 1000
                        }}
                    >
                        {aiConnect && (
                            <div>AI未连接</div>
                        )}
                        <textarea 
                            placeholder="请输入..." 
                            rows="4" 
                            className="textarea_input"
                        ></textarea>
                        <br />
                        <button className="check-btn">审计</button> 
                        <button className="close-btn" onClick={handleCloseClick}>关闭</button>
                        <br />
                        <textarea 
                            className="textarea_hidden" 
                            id={`textarea_hidden_${issue.id}`} 
                            hidden="true"
                        ></textarea>
                        <div className="result" id={`ai_check_${issue.id}`}></div>
                    </div>
                )}
            </div>

            <style>
                {`
                .textarea_input {
                    width: 900px;
                    height: 100px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    padding: 8px;
                    font-family: inherit;
                    resize: vertical;
                }
                    
                .close-btn {
                    margin-top: 10px;
                    padding: 5px 10px;
                    background-color: #f44336;
                    color: #fff;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    margin-right: 10px;
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
                
                .ai_float {
                    background: white;
                    padding: 15px;
                    border-radius: 8px;
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                    border: 1px solid #e0e0e0;
                }
                
                `}
            </style>
        </>
    );
}

export default AiCheckTemplate;