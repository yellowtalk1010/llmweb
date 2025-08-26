import { Fragment, useState, useEffect, useRef } from "react";

function AiCheckTemplate({ issue }) {
    console.info("ai checker: ");
    console.info(issue);
    
    const [isOpen, setIsOpen] = useState(false);
    const [position, setPosition] = useState({ top: 0, left: 0 });
    const linkRef = useRef(null);
    const floatRef = useRef(null);
    
    const handleAiCheckClick = () => {
        if (linkRef.current) {
            const rect = linkRef.current.getBoundingClientRect();
            setPosition({
                top: rect.bottom + window.scrollY,
                left: rect.left + window.scrollX
            });
        }
        setIsOpen(true);
    };
    
    const handleCloseClick = () => {
        setIsOpen(false);
    };
    
    // 点击外部区域关闭悬浮窗
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (isOpen && 
                floatRef.current && 
                !floatRef.current.contains(event.target) && 
                !event.target.closest('.ai_check')) {
                setIsOpen(false);
            }
        };
        
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    return (
        <>
            <div style={{ position: 'relative', display: 'inline-block' }}>
                <a 
                    ref={linkRef}
                    className="ai_check" 
                    onClick={handleAiCheckClick}
                    style={{ cursor: 'pointer', textDecoration: 'none'}}
                >
                    AI审计
                </a>

                
                
                {isOpen && (
                    <div 
                        ref={floatRef}
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
                            className="textarea"
                        ></textarea>
                        <br />
                        <button className="check-btn">审计</button> 
                        <button className="close-btn" onClick={handleCloseClick}>关闭</button>
                        <br />
                        <textarea 
                            className="textarea_hidden" 
                            id={`textarea_hidden-${issue.id}`} 
                            hidden
                        ></textarea>
                        <div className="result" id={`ai_check_${issue.id}`}></div>
                    </div>
                )}
            </div>

            <style>
                {`
                .textarea {
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
                
                .result {
                    margin-top: 10px;
                    padding: 10px;
                    border: 1px solid #e0e0e0;
                    border-radius: 4px;
                    background-color: #f9f9f9;
                    min-height: 50px;
                }
                `}
            </style>
        </>
    );
}

export default AiCheckTemplate;