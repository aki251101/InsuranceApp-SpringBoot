document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-edit-target]").forEach((button) => {
    const form = document.getElementById(button.dataset.editTarget);
    if (!form) {
      return;
    }

    button.addEventListener("click", () => {
      const willOpen = form.hidden;
      form.hidden = !willOpen;
      button.setAttribute("aria-expanded", String(willOpen));
    });
  });

  document.querySelectorAll("[data-confirm-datetime]").forEach((input) => {
    input.addEventListener("change", () => {
      input.blur();
    });
  });
});
