import './../../index.css';
import { useEffect, useRef, useState } from "react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

/***
 * 股票分析系统前端展示首页
 */
function Stock() {
  // 从接口获取页面列表
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const leftPanelRef = useRef(null);
  const [collapsed, setCollapsed] = useState(false);
  const [isHovering, setIsHovering] = useState(false);

  const [openedPages, setOpenedPages] = useState([]);
  const [activePageId, setActivePageId] = useState(null);
  const [iframeSrcMap, setIframeSrcMap] = useState({});

  // 组件加载时请求接口
  useEffect(() => {
    const controller = new AbortController();

    const fetchPages = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await fetch("/pages/list", {
          method: "GET",
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`请求失败：HTTP ${response.status}`);
        }

        const data = await response.json();

        // 兼容接口直接返回数组，或者返回 { data: [...] }
        const pageList = Array.isArray(data) ? data : data.data;

        if (!Array.isArray(pageList)) {
          throw new Error("接口返回的数据格式不正确");
        }

        setPages(pageList);
      } catch (err) {
        if (err.name !== "AbortError") {
          console.error("获取页面列表失败：", err);
          setError(err.message || "获取页面列表失败");
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    fetchPages();

    return () => controller.abort();
  }, []);

  const toggleLeftPanel = (e) => {
    e.stopPropagation();

    if (collapsed) {
      leftPanelRef.current?.expand();
    } else {
      leftPanelRef.current?.collapse();
    }

    setCollapsed(!collapsed);
  };

  const openPage = (page) => {
    setOpenedPages((prev) =>
      prev.some((p) => p.id === page.id) ? prev : [...prev, page]
    );

    setIframeSrcMap((prev) => ({
      ...prev,
      [page.id]: prev[page.id] || page.url,
    }));

    setActivePageId(page.id);
  };

  const closePage = (id) => {
    setOpenedPages((prev) => {
      const remaining = prev.filter((p) => p.id !== id);

      if (activePageId === id) {
        const closedIndex = prev.findIndex((p) => p.id === id);
        const nextPage =
          remaining[Math.min(closedIndex, remaining.length - 1)];

        setActivePageId(nextPage?.id ?? null);
      }

      return remaining;
    });

    setIframeSrcMap((prev) => {
      const newMap = { ...prev };
      delete newMap[id];
      return newMap;
    });
  };

  const refreshPage = (id) => {
    setIframeSrcMap((prev) => {
      const src = prev[id];
      if (!src) return prev;

      const url = new URL(src, window.location.href);
      url.searchParams.set("_refresh", Date.now());

      return {
        ...prev,
        [id]: url.href,
      };
    });
  };

  return (
    <PanelGroup direction="horizontal" style={{ height: "100vh" }}>
      <Panel
        ref={leftPanelRef}
        collapsible
        defaultSize={10}
        minSize={5}
      >
        {loading ? (
          <div style={{ padding: 10 }}>正在加载页面列表...</div>
        ) : error ? (
          <div style={{ padding: 10, color: "red" }}>
            {error}
            <button
              onClick={() => window.location.reload()}
              style={{ marginLeft: 8 }}
            >
              重试
            </button>
          </div>
        ) : (
          <ul style={{ margin: 0, padding: 10, listStyle: "none" }}>
            {pages.map((page) => (
              <li key={page.id} style={{ margin: "8px 0" }}>
                <a
                  href="#!"
                  onClick={(e) => {
                    e.preventDefault();
                    openPage(page);
                  }}
                  style={{
                    textDecoration: "none",
                    color: "#007bff",
                    cursor: "pointer",
                  }}
                >
                  {page.title}
                </a>
              </li>
            ))}
          </ul>
        )}
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

      <Panel defaultSize={90}>
        <div
          style={{
            height: "100%",
            display: "flex",
            flexDirection: "column",
          }}
        >
          {/* 标签页 */}
          {openedPages.length > 0 && (
            <div
              style={{
                display: "flex",
                borderBottom: "1px solid #ccc",
              }}
            >
              {openedPages.map((page) => (
                <div
                  key={page.id}
                  className={`px-3 py-1 cursor-pointer border-r flex items-center ${
                    page.id === activePageId
                      ? "bg-green-100"
                      : "bg-white"
                  }`}
                >
                  <span onClick={() => setActivePageId(page.id)}>
                    {page.title}
                  </span>

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

          {/* iframe 区域 */}
          <div style={{ flex: 1, position: "relative" }}>
            {openedPages.map((page) => (
              <iframe
                key={page.id}
                src={iframeSrcMap[page.id]}
                title={page.title}
                style={{
                  display:
                    page.id === activePageId ? "block" : "none",
                  width: "100%",
                  height: "100%",
                  border: "none",
                  position: "absolute",
                  top: 0,
                  left: 0,
                }}
              />
            ))}

            {openedPages.length === 0 && (
              <div style={{ padding: 20 }}>
                {loading
                  ? "正在加载页面列表..."
                  : "请选择一个网页打开"}
              </div>
            )}
          </div>
        </div>
      </Panel>
    </PanelGroup>
  );
}

export default Stock;