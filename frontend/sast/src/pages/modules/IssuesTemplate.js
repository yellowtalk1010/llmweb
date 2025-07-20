import {Fragment, useState, useEffect } from "react"

/**
 * @param: issueDatas 是跟随 文件返回 issue列表数据
 */
function IssuesTemplate({ issueDatas }) {
  // console.info("触发issues模块渲染")
  // console.info(issueDatas)

  //点击 trace a 标签
  const [top, setTop] = useState(0);

  if(issueDatas==null || Array.from(issueDatas).length==0){
    return null;
  }

  // const [otherSourceCodeData, setOtherSourceCodeData] = useState({
  //   lines:[]
  // })
  // function openFloatingFile(event, trace){
  //   const file = trace.file
  //   //打开一个新文件，展示在div中
  //   const filePath = document.getElementById("floating-file-name").textContent
  //   console.info("filePath:" + filePath)
  //   if(filePath=="" || filePath!=file){
  //     setOtherSourceCodeData({
  //       lines: [],
  //       status: 200
  //     })
  //     document.getElementById("floating-file-name").innerText = file
  //     //加载源文件
  //     fetch('/otherSourceCode_list?file='+file, {
  //       method: 'GET',
  //       headers: {
  //         'Content-Type': 'application/json'
  //       }
  //     }).then(res =>{
  //       const json = res.json();
  //       // console.info(json)
  //       return json
  //     }).then(data =>{
  //       // console.log("rule_vtid 的数据")
  //       console.log(data)
  //       setOtherSourceCodeData({
  //         lines: data,
  //         status: 200
  //       })
  //       // console.info("渲染完成后执行")
  //     }).catch(e =>{
  //       console.log(e)
  //     })
  //   }

  //   const rect = event.target.getBoundingClientRect();
  //   setTop(rect.bottom + window.scrollY + 5); // 按钮下方 +10px

  //   var floatingFileDom = document.getElementById("floating-file")
  //   console.info(floatingFileDom)
  //   floatingFileDom.classList.remove("floating-file-hidden")
  //   floatingFileDom.classList.add("floating-file-show")

  //   console.info(document.getElementById("otherfileline_26"))
  //   const otherFileLiDom = document.getElementById("otherfileline_" + trace.line)
  //   if(otherFileLiDom){
  //     const containerHeight = floatingFileDom.clientHeight;
  //     const liOffsetTop = otherFileLiDom.offsetTop;
  //     const liHeight = otherFileLiDom.offsetHeight;
  //     // 计算 li 要滚动到的位置，使其垂直居中
  //     const scrollTop = liOffsetTop - (containerHeight / 2) + (liHeight / 2);
  //     // 平滑滚动到目标位置
  //     floatingFileDom.scrollTo({
  //       top: scrollTop,
  //       behavior: 'smooth'
  //     });

  //     //将li标签加粗
  //     otherFileLiDom.classList.add('sourcecode-li');
  
  //   }

  // }


  
  function link(event, currentFile, trace){
    console.info(trace)
    document.getElementById("sourcecode_file").querySelectorAll('div a.trace_a_click').forEach(li => {
      li.classList.remove('trace_a_click');
    });
    document.getElementById("sourcecode_file").querySelectorAll('div li.sourcecode-li').forEach(li => {
      li.classList.remove('sourcecode-li');
    });

    event.target.classList.add('trace_a_click')

    const toId = trace.id
    const toFile = trace.file
    if(currentFile==toFile){
      //closeFloatingFile()
      //同一个文件中
      // const el = document.getElementById("line_" + trace.line)
      // el.scrollIntoView({
      //   behavior: 'smooth',      // 平滑滚动
      //   block: 'center',         // 垂直方向：滚动到中间
      //   inline: 'center'         // 水平方向：滚动到中间
      // });

      // el.classList.add('sourcecode-li')
      
      console.info("统一文件中打开")
    }
    else{
      //不同文件中打开
      // openFloatingFile(event, trace)
      console.info("不同文件中打开")
    }

    
      
  }

  //渲染issue
  function renderIssue(issue) {
    return (
      <div key={issue.id} id={issue.id} className="issueDiv">
        <div>{issue.name}</div>
        <div>
          {issue.line}/{issue.vtId}/{issue.rule}/{issue.defectLevel}/{issue.defectType}
        </div>
        <div>{issue.ruleDesc}</div>
        <div>{issue.issueDesc}</div>
        <div>
        {
          issue.traces.map((trace, traceIndex) => (
            <div>
              <a className="trace_a" onClick={(event)=>link(event, issue.filePath, trace)} >{trace.file} # {trace.line} # {trace.message}</a>
            </div>
          ))
        }
        </div>
      </div>
    );
  }


  return issueDatas != null
    ? issueDatas.map((issue) => (
        renderIssue(issue)
      ))
    : null;
}


export default IssuesTemplate;
