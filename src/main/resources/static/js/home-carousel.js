document.addEventListener("DOMContentLoaded", () => {
  const carousel = document.querySelector(".banner-carousel");
  if (!carousel) {
    return;
  }

  const track = carousel.querySelector(".carousel-track");
  const viewport = carousel.querySelector(".carousel-viewport");
  const previousButton = carousel.querySelector(".carousel-control-prev");
  const nextButton = carousel.querySelector(".carousel-control-next");
  const indicators = carousel.querySelector(".carousel-indicators");
  const status = carousel.querySelector(".carousel-status");
  const originalSlides = Array.from(track.children);

  if (originalSlides.length < 2) {
    return;
  }

  const intervalMilliseconds = 7000;
  const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
  const firstClone = originalSlides[0].cloneNode(true);
  const lastClone = originalSlides[originalSlides.length - 1].cloneNode(true);

  firstClone.dataset.clone = "true";
  lastClone.dataset.clone = "true";
  firstClone.setAttribute("aria-hidden", "true");
  lastClone.setAttribute("aria-hidden", "true");
  track.append(firstClone);
  track.prepend(lastClone);

  const slides = Array.from(track.children);
  let trackIndex = 1;
  let timerId;
  let isAnimating = false;
  let pointerStartX = 0;
  let pointerDeltaX = 0;

  originalSlides.forEach((slide, index) => {
    slide.dataset.slideIndex = String(index);

    const indicator = document.createElement("button");
    indicator.type = "button";
    indicator.className = "carousel-indicator";
    indicator.setAttribute("aria-label", `${index + 1}枚目のバナーを表示`);
    indicator.addEventListener("click", () => {
      goToSlide(index);
      restartTimer();
    });
    indicators.append(indicator);
  });

  const indicatorButtons = Array.from(indicators.children);

  function getRealIndex() {
    return (trackIndex - 1 + originalSlides.length) % originalSlides.length;
  }

  function updatePresentation(announce = false) {
    const realIndex = getRealIndex();

    slides.forEach((slide, index) => {
      const isActive = index === trackIndex;
      slide.classList.toggle("is-active", isActive);
      slide.setAttribute("aria-hidden", String(!isActive));
    });

    indicatorButtons.forEach((indicator, index) => {
      const isActive = index === realIndex;
      indicator.classList.toggle("is-active", isActive);
      indicator.setAttribute("aria-current", isActive ? "true" : "false");
    });

    if (announce) {
      status.textContent = `${realIndex + 1}枚目のバナーを表示中`;
    }
  }

  function moveTrack({ animate = true, announce = false } = {}) {
    track.classList.toggle("is-resetting", !animate);
    track.style.transform = `translateX(-${trackIndex * 100}%)`;
    updatePresentation(announce);

    if (!animate) {
      requestAnimationFrame(() => {
        track.classList.remove("is-resetting");
      });
    }
  }

  function moveBy(direction) {
    if (isAnimating) {
      return;
    }

    isAnimating = true;
    trackIndex += direction;
    moveTrack({ announce: true });
    settleWithoutAnimation();
  }

  function goToSlide(realIndex) {
    if (isAnimating || realIndex === getRealIndex()) {
      return;
    }

    isAnimating = true;
    trackIndex = realIndex + 1;
    moveTrack({ announce: true });
    settleWithoutAnimation();
  }

  function settleWithoutAnimation() {
    if (!reducedMotion.matches) {
      return;
    }

    if (trackIndex === slides.length - 1) {
      trackIndex = 1;
      moveTrack({ animate: false });
    } else if (trackIndex === 0) {
      trackIndex = originalSlides.length;
      moveTrack({ animate: false });
    }

    isAnimating = false;
  }

  function stopTimer() {
    window.clearInterval(timerId);
    timerId = undefined;
  }

  function startTimer() {
    if (reducedMotion.matches || timerId) {
      return;
    }

    timerId = window.setInterval(() => {
      moveBy(1);
    }, intervalMilliseconds);
  }

  function restartTimer() {
    stopTimer();
    startTimer();
  }

  track.addEventListener("transitionend", (event) => {
    if (event.target !== track || event.propertyName !== "transform") {
      return;
    }

    if (trackIndex === slides.length - 1) {
      trackIndex = 1;
      moveTrack({ animate: false });
    } else if (trackIndex === 0) {
      trackIndex = originalSlides.length;
      moveTrack({ animate: false });
    }

    isAnimating = false;
  });

  previousButton.addEventListener("click", () => {
    moveBy(-1);
    restartTimer();
  });

  nextButton.addEventListener("click", () => {
    moveBy(1);
    restartTimer();
  });

  carousel.addEventListener("mouseenter", stopTimer);
  carousel.addEventListener("mouseleave", startTimer);
  carousel.addEventListener("focusin", stopTimer);
  carousel.addEventListener("focusout", (event) => {
    if (!carousel.contains(event.relatedTarget)) {
      startTimer();
    }
  });

  viewport.addEventListener("pointerdown", (event) => {
    if (event.pointerType === "mouse") {
      return;
    }

    pointerStartX = event.clientX;
    pointerDeltaX = 0;
    stopTimer();
    viewport.setPointerCapture(event.pointerId);
  });

  viewport.addEventListener("pointermove", (event) => {
    if (!viewport.hasPointerCapture(event.pointerId)) {
      return;
    }
    pointerDeltaX = event.clientX - pointerStartX;
  });

  viewport.addEventListener("pointerup", (event) => {
    if (viewport.hasPointerCapture(event.pointerId)) {
      viewport.releasePointerCapture(event.pointerId);
    }

    if (Math.abs(pointerDeltaX) >= 45) {
      moveBy(pointerDeltaX < 0 ? 1 : -1);
    }

    pointerStartX = 0;
    pointerDeltaX = 0;
    startTimer();
  });

  viewport.addEventListener("pointercancel", () => {
    pointerStartX = 0;
    pointerDeltaX = 0;
    startTimer();
  });

  reducedMotion.addEventListener("change", () => {
    stopTimer();
    startTimer();
  });

  moveTrack({ animate: false });
  startTimer();
});
