import { useRef, useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

function HelloWorld() {
  const leftPanelRef = useRef(null);
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [isHovering, setIsHovering] = useState(false);

  const toggleLeftPanel = (e) => {
    e.stopPropagation();
    if (isCollapsed) {
      leftPanelRef.current.expand();
    } else {
      leftPanelRef.current.collapse();
    }
    setIsCollapsed(!isCollapsed);
  };

  return (
    <PanelGroup direction="horizontal" style={{ height: "100vh" }}>
      <Panel
        ref={leftPanelRef}
        collapsible
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
          {isCollapsed ? "→" : "←"}
        </div>
      </PanelResizeHandle>

      <Panel defaultSize={70}>Right Content</Panel>
    </PanelGroup>
  );
}

export default HelloWorld;