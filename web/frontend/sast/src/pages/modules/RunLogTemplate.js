import { useState, useEffect } from "react";


import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import 'react-tabs/style/react-tabs.css';

function RunLogTemplate() {
  const [logs, setLogs] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [socket, setSocket] = useState(null);

  useEffect(() => {
    // 创建 WebSocket 连接
    const ws = new WebSocket("ws://localhost:8080/log");
    setSocket(ws);

    ws.addEventListener('open', () => {
      console.log('WebSocket 连接已打开');
      setIsConnected(true);
    });

    ws.addEventListener('message', (event) => {
      // 处理接收到的日志消息
      const newLog = event.data;
      setLogs(prevLogs => [...prevLogs, newLog]);
    });

    ws.addEventListener('error', (err) => {
      console.error('WebSocket 连接出错', err);
      setIsConnected(false);
    });

    ws.addEventListener('close', () => {
      console.log('WebSocket 连接已关闭');
      setIsConnected(false);
    });

    // 组件卸载时关闭连接
    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, []);

  // 自动滚动到底部
  useEffect(() => {
    const logContainer = document.getElementById('log-container');
    if (logContainer) {
      logContainer.scrollTop = logContainer.scrollHeight;
    }
  }, [logs]);

  return (
    <>
      <div>
        <h2>实时日志</h2>
            <div style={{ 
              marginBottom: '10px',
              color: isConnected ? 'green' : 'red',
              fontWeight: 'bold'
            }}>
              连接状态: {isConnected ? '已连接' : '未连接'}
            </div>
      </div>
      <Tabs>
        <TabList>
            <Tab>log</Tab>
            <Tab>error</Tab>
        </TabList>

        <TabPanel>
          <div style={{ padding: '20px', minWidth: '800px', width: '90%', margin: '0 auto' }}>

            <div id="log-container" className={"log"}>
              {logs.length > 0 ? (
                logs.map((log, index) => (
                  <div key={index} style={{ marginBottom: '4px' }}>
                    {log}
                  </div>
                ))
              ) : (
                <div>等待日志数据...</div>
              )}
            </div>
          </div>
        </TabPanel>
        <TabPanel>
           <div style={{ padding: '20px', minWidth: '800px', width: '90%', margin: '0 auto' }}>

            <div id="log-container" className={"log"}>
              {logs.length > 0 ? (
                logs.map((log, index) => (
                  <div key={index} style={{ marginBottom: '4px' }}>
                    {log}
                  </div>
                ))
              ) : (
                <div>等待错误数据...</div>
              )}
            </div>
          </div>   
        </TabPanel>
      </Tabs>

      <style>
        {
          `
            div .log {
              height: 500px;
              overflow-y: auto;
              background-color: #1e1e1e;
              color: #d4d4d4;
              padding: 10px;
              border-radius: 4px;
              font-family: monospace;
              white-space: pre-wrap;
              word-break: break-all;
            }
          `
        }
      </style>

    </>

    

    
  );
}

export default RunLogTemplate;