import { useState } from "react";

function AiCheckTemplate({ issue }) {
    console.info("ai checker: ");
    console.info(issue);
    
    const [isOpen, setIsOpen] = useState(false);
    
    const handleAiCheckClick = () => {
        setIsOpen(true);
    };
    
    const handleCloseClick = () => {
        setIsOpen(false);
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
                
                .ai_check {
                    cursor: pointer;
                    color: #1976d2;
                    text-decoration: underline;
                }
                
                .ai_check:hover {
                    color: #1565c0;
                }
                
                `}
            </style>
        </>
    );
}

export default AiCheckTemplate;