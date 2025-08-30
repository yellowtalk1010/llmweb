import { useRef, useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

function Stock() {
  const pages = [
    { id: "1", title: "stockapi", url: "https://stockapi.com.cn/" },
    { id: "2", title: "招商", url: "https://xtrade.newone.com.cn/ssologin?t=jykstd" },
    { id: "3", title: "全部", url: "AllStock" },
    { id: "4", title: "我的", url: "MyStock" },
  ];

  const leftPanelRef = useRef(null);
  const [collapsed, setCollapsed] = useState(false);
  const [isHovering, setIsHovering] = useState(false);

  const [openedPages, setOpenedPages] = useState([]);
  const [activePageId, setActivePageId] = useState(null);
  const [iframeSrcMap, setIframeSrcMap] = useState({});

  const toggleLeftPanel = (e) => {
    e.stopPropagation();
    if (collapsed) {
      leftPanelRef.current.expand();
    } else {
      leftPanelRef.current.collapse();
    }
    setCollapsed(!collapsed);
  };

  const openPage = (page) => {
    if (!openedPages.find((p) => p.id === page.id)) {
      setOpenedPages([...openedPages, page]);
      setIframeSrcMap((prev) => ({ ...prev, [page.id]: page.url }));
    }
    setActivePageId(page.id);
  };

  const closePage = (id) => {
    setOpenedPages((prev) => prev.filter((p) => p.id !== id));
    setIframeSrcMap((prev) => {
      const newMap = { ...prev };
      delete newMap[id];
      return newMap;
    });
    if (activePageId === id) {
      setActivePageId(openedPages.length > 1 ? openedPages[0].id : null);
    }
  };

  const refreshPage = (id) => {
    setIframeSrcMap((prev) => ({
      ...prev,
      [id]: `${prev[id].split("?")[0]}?t=${Date.now()}`,
    }));
  };

  return (
    <PanelGroup direction="horizontal" style={{ height: "100vh" }}>
      <Panel ref={leftPanelRef} collapsible defaultSize={15} minSize={8}>
        <ul style={{ margin: 0, padding: 10, listStyle: "none" }}>
          {pages.map((page) => (
            <li key={page.id} style={{ margin: "8px 0" }}>
              <a
                href="#!"
                onClick={(e) => {
                  e.preventDefault();
                  openPage(page);
                }}
                style={{ textDecoration: "none", color: "#007bff", cursor: "pointer" }}
              >
                {page.title}
              </a>
            </li>
          ))}
        </ul>
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
          {collapsed ? "▶️" : "◀️"}
        </div>
      </PanelResizeHandle>

      <Panel defaultSize={85}>
        <div style={{ height: "100%", display: "flex", flexDirection: "column" }}>
          {/* 标签页 */}
          {openedPages.length > 0 && (
            <div style={{ display: "flex", borderBottom: "1px solid #ccc" }}>
              {openedPages.map((page) => (
                <div
                  key={page.id}
                  style={{
                    padding: "5px 10px",
                    cursor: "pointer",
                    backgroundColor: page.id === activePageId ? "#ddd" : "#f5f5f5",
                    borderRight: "1px solid #ccc",
                    display: "flex",
                    alignItems: "center",
                  }}
                >
                  <span onClick={() => setActivePageId(page.id)}>{page.title}</span>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      refreshPage(page.id);
                    }}
                    style={{ marginLeft: 5, border: "none", background: "transparent", cursor: "pointer" }}
                    title="刷新页面"
                  >
                    🔄
                  </button>
                  <button
                    onClick={() => closePage(page.id)}
                    style={{ marginLeft: 5, border: "none", background: "transparent", cursor: "pointer" }}
                    title="关闭页面"
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* iframe 区域，全部渲染，切换显示 */}
          <div style={{ flex: 1, position: "relative" }}>
            {openedPages.map((page) => (
              <iframe
                key={page.id}
                src={iframeSrcMap[page.id]}
                title={page.title}
                style={{
                  display: page.id === activePageId ? "block" : "none",
                  width: "100%",
                  height: "100%",
                  border: "none",
                  position: "absolute",
                  top: 0,
                  left: 0,
                }}
              />
            ))}
            {openedPages.length === 0 && <div style={{ padding: 20 }}>请选择一个网页打开</div>}
          </div>
        </div>
      </Panel>
    </PanelGroup>
  );
}

export default Stock;
