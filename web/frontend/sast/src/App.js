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
import Flow from "./flows/Flow";

function App() {

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

          <Route path="/flows/Flow" element={<Flow />}/>

        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
