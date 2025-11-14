const fpsSpan = document.getElementById("fps") as HTMLSpanElement;
const resSpan = document.getElementById("res") as HTMLSpanElement;

let frames = 0;
let last = performance.now();

function loop() {
    frames++;
    const now = performance.now();
    if (now - last >= 1000) {
        fpsSpan.textContent = frames.toString();
        frames = 0;
        last = now;
    }
    requestAnimationFrame(loop);
}

resSpan.textContent = "640 x 480"; // static resolution for demo
loop();
