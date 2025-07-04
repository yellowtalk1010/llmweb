import {Fragment, useState } from "react"

import { BrowserRouter ,Routes,  Router, Route, Link  } from 'react-router-dom';

import Files from "./pages/Files";
import File from "./pages/File";
import Rules from "./pages/Rules";
import Rule from "./pages/Rule";
import LoadConfig from "./pages/LoadConfig"; //加载配置文件
import SourceCode from "./pages/SourceCode"

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
          <Route path="/" element={<LoadConfig />} />
          
          <Route path="/pages/Rules" element={<Rules />} />
          <Route path="/pages/Rule" element={<Rule />} />

          <Route path="/pages/Files" element={<Files />}/>
          <Route path="/pages/File" element={<File />}/>

          <Route path="/pages/SourceCode" element={<SourceCode />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
