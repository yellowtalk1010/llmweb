import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";



function HelloWorld() {

  const [leftCollapsed, setLeftCollapsed] = useState(false);
  const [isHovering, setIsHovering] = useState(false);

  const toggleLeftPanel = (e) => {
    console.info("点击事件")
    e.stopPropagation(); // 阻止事件冒泡到拖拽手柄
    setLeftCollapsed(!leftCollapsed);
  };

  return (
    <PanelGroup direction="horizontal" style={{ height: "100vh" }}>
      <Panel 
        collapsible={true}
        collapsed={"false"}
        defaultSize={30} 
        minSize={8}
      >
        Left Content
      </Panel>

      <PanelResizeHandle 
        style={{
          width: "12px",
          backgroundColor: isHovering ? "#e5e7eb" : "#f3f4f6",
          position: "relative",
          transition: "background-color 0.2s",
        }}
        onMouseEnter={() => setIsHovering(true)}
        onMouseLeave={() => setIsHovering(false)}
      >
        {/* 折叠/展开图标 */}
        <div
          onClick={toggleLeftPanel}
          style={{
            position: "absolute",
            left: "50%",
            top: "50%",
            transform: "translate(-50%, -50%)",
            cursor: "pointer",
            padding: "4px",
            borderRadius: "4px",
            backgroundColor: isHovering ? "#d1d5db" : "transparent",
          }}
        >
          

          {leftCollapsed ? "→" : "←"}
        </div>
      </PanelResizeHandle>

      <Panel defaultSize={70}>
        Right Content
      </Panel>
    </PanelGroup>
  );
}

export default HelloWorld;