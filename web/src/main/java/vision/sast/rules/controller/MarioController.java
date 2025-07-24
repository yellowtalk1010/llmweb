package vision.sast.rules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;


@RestController
public class MarioController {

    public static String RUN_TOKEN = "";

    /***
     * 马力欧页面
     * @return
     */
    @GetMapping("mario")
    public String mario() throws Exception {

        RUN_TOKEN = UUID.randomUUID().toString();
        String config_url  = "config";
        String start_url = "start?token=" + RUN_TOKEN;
        String html =
                """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                          <meta charset="UTF-8">
                          <title>马里奥小游戏</title>
                          <style>
                            body {
                              margin: 0;
                              overflow: hidden;
                              background: skyblue;
                            }
                            #game {
                              position: relative;
                              width: 100vw;
                              height: 100vh;
                              background: linear-gradient(skyblue 70%, green 30%);
                            }
                            #mario {
                              position: absolute;
                              bottom: 30px;
                              left: 50px;
                              width: 100px;
                              height: 100px;
                              background: url('{{{mario}}}') no-repeat center/cover;
                            }
                            #goomba {
                              position: absolute;
                              bottom: 30px;
                              left: 100vw;
                              width: 60px;
                              height: 60px;
                              background: url('{{{goomba}}}') no-repeat center/cover;
                            }
                          </style>
                        </head>
                        <body>
                          <div id="game">
                            <div>
                                <ul>
                                  <li><a style="text-decoration: none;" href='{{{config_url}}}'>🔥上传配置文件🔥</a></li>
                                  <br>
                                  <li title="踩死蘑菇，开启马力欧奥德赛">👨‍🔧启动🚀启动🚀🚀启动🚀🚀🚀马力欧.奥德赛💪💪💪💪</li>
                              <ul>
                            </div>
                            <div id="mario"></div>
                            <div id="goomba"></div>
                          </div>
                                        
                          <script>
                            const mario = document.getElementById("mario");
                            const goomba = document.getElementById("goomba");
                                        
                            let marioX = 50;
                            let marioY = 0;
                            let velocityY = 0;
                            let isJumping = false;
                                        
                            // Goomba 自动向左移动
                            let goombaX = window.innerWidth;
                                        
                            function jump() {
                              if (!isJumping) {
                                velocityY = 15;
                                isJumping = true;
                              }
                            }
                                        
                            document.addEventListener("keydown", (e) => {
                              if (e.key === " " || e.key === "ArrowUp") jump();
                              if (e.key === "ArrowRight") marioX += 10;
                              if (e.key === "ArrowLeft") marioX -= 10;
                            });
                                        
                            function detectCollision() {
                              const marioRect = mario.getBoundingClientRect();
                              const goombaRect = goomba.getBoundingClientRect();
                                        
                              const verticalCheck = (marioRect.bottom <= goombaRect.bottom + 10) &&
                                                    (marioRect.bottom >= goombaRect.top - 10);
                              const horizontalCheck = marioRect.right > goombaRect.left &&
                                                      marioRect.left < goombaRect.right;
                                        
                              if (verticalCheck && horizontalCheck && velocityY < 0) {
                                // 踩中了，跳转页面
                                window.location.href = "{{{start_url}}}";
                              }
                            }
                                        
                            function update() {
                              // 更新马里奥跳跃
                              marioY += velocityY;
                              velocityY -= 1;
                              if (marioY <= 0) {
                                marioY = 0;
                                velocityY = 0;
                                isJumping = false;
                              }
                                        
                              mario.style.bottom = 30 + marioY + "px";
                              mario.style.left = marioX + "px";
                                        
                              // 更新蘑菇位置，用来控制蘑菇移动速度
                              goombaX -= 2; 
                              if (goombaX < -50) goombaX = window.innerWidth;
                              goomba.style.left = goombaX + "px";
                                        
                              detectCollision();
                                        
                              requestAnimationFrame(update);
                            }
                                        
                            update();
                          </script>
                        </body>
                        </html>
                                        
                
                
                """;
        html = html.replace("{{{config_url}}}", config_url);
        html = html.replace("{{{start_url}}}", start_url);

        String mario = getImage("mario.png");
        html = html.replace("{{{mario}}}", mario);

        String goomba = getImage("goomba.png");
        html = html.replace("{{{goomba}}}", goomba);
        return html;
    }


    public static String getImage(String png) throws Exception {
        URL url = MarioController.class.getClassLoader().getResource("images/" + png);
        InputStream inputStream = MarioController.class.getClassLoader().getResourceAsStream("images/" + png);

//        File file = new File("images/" + png); //mario   mg
//        System.out.println(file.getAbsolutePath());
//        byte[] gifBytes = Files.readAllBytes(file.toPath());
        byte[] gifBytes = inputStream.readAllBytes();
        String base64 = Base64.getEncoder().encodeToString(gifBytes);
        String image = "data:image/gif;base64," + base64;
        return image;
    }

}