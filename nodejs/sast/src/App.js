import {Fragment, useState } from "react"

import { BrowserRouter ,Routes,  Router, Route, Link  } from 'react-router-dom';

import Page1 from './pages/Page1';
import Page2 from "./pages/Page2";
import IssueResultFile from "./pages/IssueResultFile"; //issue结果文件

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
          <Route path="/" element={<IssueResultFile />} />
          <Route path="/pages/Page1" element={<Page1 />} />
          <Route path="/pages/Page2" element={<Page2 />}/>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
