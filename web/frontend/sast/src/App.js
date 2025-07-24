import {Fragment, useState } from "react"

import { BrowserRouter ,Routes,  Router, Route, Link  } from 'react-router-dom';

import AllFiles from "./pages/AllFiles";
import File from "./pages/File";
import AllRules from "./pages/AllRules";
import Rule from "./pages/Rule";
import HelloWorld from "./pages/HelloWorld"; //加载配置文件
import SourceCode from "./pages/SourceCode"
import Start from "./pages/Start";
import FunctionModule from "./pages/FunctionModule"; //函数模型

function App() {

  // const [data, setData] = useState({
  //   title: '默认标题',
  //   content: '默认内容'
  // })
  // function handleClick(event){

  //   console.info(Page1)

  //   fetch('/get1', {
  //     method: 'GET',
  //     headers: {
  //       'Content-Type': 'application/json'
  //     }
  //   }).then(res =>{
  //     // console.info(res.json)
  //     return res.json();
  // }).then(data =>{
  //     console.log("get请求的数据")
  //     console.log(data)
  //     console.log(data.name)
  // }).catch(e =>{
  //     console.log(e)
  // })
 

  //   console.info(event)

  //   setData({
  //     ...data,
  //     title: "新标题"
  //   })
  // }

  

  return (
    <div className="App" >
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HelloWorld />} />
          
          <Route path="/pages/AllRules" element={<AllRules />} />
          <Route path="/pages/Rule" element={<Rule />} />

          <Route path="/pages/AllFiles" element={<AllFiles />}/>
          <Route path="/pages/File" element={<File />}/>

          <Route path="/pages/SourceCode" element={<SourceCode />} />
          <Route path="/pages/Start" element={<Start />} />
          <Route path="/pages/FunctionModule" element={<FunctionModule />} />

        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
