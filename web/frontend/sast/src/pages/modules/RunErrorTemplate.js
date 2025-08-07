import {Fragment, useState, useEffect } from "react"

function RunErrorTemplate() {

     var websocketResult = null;
    // 创建 WebSocket 连接（仅一个连接复用）
    const socket = new WebSocket("ws://localhost:8080/ws");
    
    socket.addEventListener('open', () => {
        console.log('RunErrorTemplate WebSocket 连接已打开');
    });

    socket.addEventListener('error', (err) => {
        console.error('RunErrorTemplate WebSocket 连接出错', err);
    });

    return (
        <div>
            error日志
        </div>
    );
}

export default RunErrorTemplate;