import {Fragment, useState, useEffect } from "react"


function RunTemplate() {

    const [formData, setFormData] = useState({
        // command: 'java -jar D:/AAAAAAAAAAAAAAAAAAAA/github/engine/vision/target/visionSAST.jar -config D:/AAAAAAAAAAAAAAAAAAAA/github/engine/vision/target/workspace1/CJ2000A/project.json',
        command: '',
        configType: '',
        fileContent: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false);

    const runCammand = async (e) => {
        e.preventDefault();
        console.info("running")
    
        if (!formData.command.trim()) {
            //setSubmitStatus({ message: '命令不能为空', isError: true });
            //return;
        }
        if (!formData.configType) {
            //setSubmitStatus({ message: '请选择配置类型', isError: true });
            //return;
        }

        setIsSubmitting(true);
    

        try {
            const response = await fetch('/run_command', {
                method: 'POST',
                headers: {
                'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            //setSubmitStatus({ message: '命令提交成功！', isError: false });
            console.log('Success:', result);
        } 
        catch (error) {
            console.error('Error:', error);
            //   setSubmitStatus({ 
            //     message: `提交失败: ${error.message || '服务器错误'}`,
            //     isError: true 
            //   });
        } 
        finally {
            setIsSubmitting(false);
        }
    };


    const handleChange = (e) => {
        console.info("change")
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };


    return (
        <>
             
                <form  >
                    <div>
                        <button 
                            onClick={runCammand}
                            disabled={isSubmitting} 
                            >
                            {isSubmitting ? '提交中...' : '运行'}
                        </button>
                    </div>
                    <div>
                        <div>
                            <span>命令:</span>
                        </div>
                        <input type='text' 
                        id="command" 
                        name="command" 
                        value={formData.command}
                        onChange={handleChange}
                        ></input>
                    </div>
                    

                    <div>
                        <div><span>配置文件:</span></div>
                        <select id="configType"
                            name="configType"
                            value={formData.configType}
                            onChange={handleChange}
                            >
                            <option value="">-- 请选择 --</option>
                            <option value="execute">执行</option>
                            <option value="validate">验证</option>
                            <option value="test">测试</option>
                            <option value="debug">调试</option>
                        </select>
                    </div>

                    <div>
                        <div>
                            <span>文件内容:</span>
                        </div>
                        <textarea name="fileContent" 
                            value={formData.fileContent}
                            onChange={handleChange}>
                        </textarea>
                    </div>
                </form>
            
            <style>
                {`
                    form {
                        font-family: Arial, sans-serif;
                        width: 90%;
                        margin: 20px auto;
                        padding: 20px;
                        border: 1px solid #ddd;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    
                    form > div {
                        margin-bottom: 20px;
                    }
                    
                    input[type='text'], select {
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
                        height: 600px; /* Approximately 100 lines */
                        min-height: 600px;
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