import {Fragment, useState } from "react"

import { BrowserRouter ,Routes,  Router, Route, Link  } from 'react-router-dom';

import AllFiles from "./pages/AllFiles";
import File from "./pages/File";
import AllRules from "./pages/AllRules";
import Rule from "./pages/Rule";
import HelloWorld from "./pages/HelloWorld"; //加载配置文件
import SourceCode from "./pages/SourceCode"
import Run from "./pages/Run";
import FunctionModule from "./pages/FunctionModule"; //函数模型
import Flow from "./flows/Flow";

import Stock from "./pages/stock/Stock";
import AllStocks from "./pages/stock/AllStocks";
import HistoryStock from "./pages/stock/HistoryStock";
import PushTushareStock from "./pages/stock/PushTushareStock";
import Moneyflow from "./pages/stock/Moneyflow";
import BuyAttention from "./pages/stock/BuyAttention";

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
          <Route path="/pages/Run" element={<Run />} />
          <Route path="/pages/FunctionModule" element={<FunctionModule />} />

          <Route path="/flows/Flow" element={<Flow />}/>
、
          {/* tushare股票信息   */}
          <Route path="/pages/Stock" element={<Stock />} />
          <Route path="/pages/AllStocks" element={<AllStocks />} />
          <Route path="/pages/HistoryStock" element={<HistoryStock/>} />
          <Route path="/pages/PushTushareStock" element={<PushTushareStock/>} />
          <Route path="/pages/Moneyflow" element= {<Moneyflow/>}/>
          <Route path="/pages/BuyAttention" element= {<BuyAttention/>}/>

        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
