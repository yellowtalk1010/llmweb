import {Fragment, useState, useEffect } from "react"

import RunLogTemplate from "./RunLogTemplate";

/***
 * 运行模版
 */
function RunTemplate() {

    const [configOptions, setConfigOptions] = useState([]);
    useEffect(() => {
        // 从后端获取配置类型
        const fetchConfigTypes = async () => {
            try {
                const response = await fetch('/command_list', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });
                const data = await response.json();
                setConfigOptions(data);
            } catch (error) {
                console.error('获取配置类型失败:', error);
            } finally {
                
            }
        };

        fetchConfigTypes();
    }, []);



    const [formData, setFormData] = useState({
        command: '',
        configType: '',
        format: ''
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
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };
    
    const handleSelect = (e) => {
        const selectVal =  e.target.value
        //console.info(selectVal)

        setFormData(prev => ({
            ...prev,
            "command": selectVal   //将选择的命令，直接写入
        }));
        
    }

    const handleFormat = async (e) => {
        const selectVal =  e.target.value
        setFormData(prev => ({
            ...prev,
            "format": selectVal
        }));

        const response = await fetch('/command_format?format=' + selectVal)
        console.info(response)
    }


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
                        <div><span>格式:</span></div>
                        <select id="format"
                            name="format"
                            value={formData.format}
                            onChange={handleFormat}
                            >
                                <option value="GBK">GBK</option>
                                <option value="UTF-8">UTF-8</option>
                            </select>
                    </div>
                    

                    <div>
                        <div><span>配置文件:</span></div>
                        <select id="configType"
                            name="configType"
                            onChange={handleSelect}
                            >
                                <option value="">--</option>
                            {
                                
                                configOptions.commands
                                ?
                                    configOptions.commands.map((option, index)=>{
                                        return (
                                            <option value={option}>
                                                {option}
                                            </option>
                                        );
                                    })
                                :
                                <></>
                            }
                        </select>
                    </div>


                    <RunLogTemplate></RunLogTemplate>

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