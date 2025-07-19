import {Fragment, useState, useEffect } from "react"

function SourceFile({node}) {

    const file = node.path
    
    const [sourceCodeData, setSourceCodeData] = useState([])

    useEffect(() => {
        fetch('/otherSourceCode_list?file='+file, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        }).then(res =>{
          const json = res.json();
          // console.info(json)
          return json
        }).then(data =>{
          // console.log("rule_vtid 的数据")
          console.log(data)
          setSourceCodeData(data)
          // console.info("渲染完成后执行")
        }).catch(e =>{
          console.log(e)
        })
    }, []);

  

    return (
        <>
            <div>
                <span>{file}</span>
            </div>

            <div id="SourceCode">
                <div id="sourcecode_file">
                    <ol>
                    {
                        sourceCodeData.map((lineHtml, index) => {
                            const newLiElement = (
                            <span
                            key={index}
                            dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                            />
                            );
                            return newLiElement
 
                        })
                    
                    }
                    </ol>
                </div>
            </div>
            
        </>
    );
}


export default SourceFile;