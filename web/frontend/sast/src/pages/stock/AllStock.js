import React, { useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

const pages = [
  { id: "1", title: "百度", url: "https://www.baidu.com" },
  { id: "2", title: "凤凰网", url: "https://www.ifeng.com" },
  { id: "3", title: "谷歌", url: "https://www.google.com" },
];

const AllStock = () => {
  const [openedPages, setOpenedPages] = useState([]); // 已打开的页面
  const [activePageId, setActivePageId] = useState(null);
  const [collapsed, setCollapsed] = useState(false); // 左侧 Panel 是否折叠

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

  const activePage = openedPages.find((p) => p.id === activePageId);

  return (
    <div style={{ height: "100vh" }}>
      <PanelGroup direction="horizontal">
        {/* 左侧 Panel */}
        <Panel defaultSize={collapsed ? 5 : 20} minSize={5} maxSize={50}>
          <div
            style={{
              padding: 10,
              height: "100%",
              boxSizing: "border-box",
              borderRight: "1px solid #ccc",
            }}
          >
            <button onClick={() => setCollapsed(!collapsed)}>
              {collapsed ? "展开" : "折叠"}
            </button>

            {!collapsed && (
              <ul style={{ marginTop: 10, listStyle: "none", padding: 0 }}>
                {pages.map((page) => (
                  <li key={page.id} style={{ margin: "5px 0" }}>
                    <button onClick={() => openPage(page)}>{page.title}</button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </Panel>

        <PanelResizeHandle />

        {/* 右侧网页内容 */}
        <Panel>
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
                      onClick={() => closePage(page.id)}
                      style={{
                        marginLeft: 5,
                        border: "none",
                        background: "transparent",
                        cursor: "pointer",
                      }}
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
    </div>
  );
};

export default AllStock;
