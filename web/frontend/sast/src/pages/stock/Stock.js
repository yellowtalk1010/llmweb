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

  const activePage = openedPages.find((p) => p.id === activePageId);

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
    }
    setActivePageId(page.id);
  };

  const closePage = (id) => {
    const newOpened = openedPages.filter((p) => p.id !== id);
    setOpenedPages(newOpened);
    if (activePageId === id) {
      setActivePageId(newOpened.length ? newOpened[newOpened.length - 1].id : null);
    }
  };

  const refreshPage = (id) => {
    setOpenedPages((prev) =>
      prev.map((p) => (p.id === id ? { ...p, refreshKey: Math.random() } : p))
    );
  };

  return (
    <PanelGroup direction="horizontal" style={{ height: "100vh" }}>
      {/* 左侧 Panel */}
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

      {/* 中间分隔线 + 收缩按钮 */}
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

      {/* 右侧 Panel */}
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

                  {/* 刷新按钮 */}
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      refreshPage(page.id);
                    }}
                    style={{
                      marginLeft: 5,
                      border: "none",
                      background: "transparent",
                      cursor: "pointer",
                    }}
                    title="刷新页面"
                  >
                    🔄
                  </button>

                  {/* 关闭按钮 */}
                  <button
                    onClick={() => closePage(page.id)}
                    style={{
                      marginLeft: 5,
                      border: "none",
                      background: "transparent",
                      cursor: "pointer",
                    }}
                    title="关闭页面"
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* 当前网页内容 */}
          <div style={{ flex: 1, overflow: "auto" }}>
            {activePage ? (
              <iframe
                key={activePage.refreshKey || activePage.id}
                src={activePage.url}
                title={activePage.title}
                style={{ width: "100%", height: "100%", border: "none" }}
              />
            ) : (
              <div style={{ padding: 20 }}>请选择一个网页打开</div>
            )}
          </div>
        </div>
      </Panel>
    </PanelGroup>
  );
}

export default Stock;
