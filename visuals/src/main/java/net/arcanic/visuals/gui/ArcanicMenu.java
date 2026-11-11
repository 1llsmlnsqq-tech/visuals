/*
 * This file is part of Arcanic Visuals...
 */
package net.arcanic.visuals.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.arcanic.visuals.core.Category;
import net.arcanic.visuals.core.Config;
import net.arcanic.visuals.core.Lang;
import net.arcanic.visuals.core.MenuPrefs;
import net.arcanic.visuals.core.Module;
import net.arcanic.visuals.core.Modules;
import net.arcanic.visuals.core.Setting;
import net.arcanic.visuals.core.Theme;
import net.arcanic.visuals.core.UiSound;
import net.arcanic.visuals.render.Anim;
import net.arcanic.visuals.render.BrandLogo;
import net.arcanic.visuals.render.Px;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ArcanicMenu extends Screen {
    private final Anim anim = new Anim();
    private float u = 1.0F;
    private int px, py, pw, ph;
    private int sideX, sideY, sideW, sideH;
    private int cx0, cw0;
    private int top, bottom;
    private long openedAt = System.currentTimeMillis();
    
    // Вкладки (взяты из твоих категорий)
    private final Category[] TABS = {Category.VISUALS, Category.UTILITY, Category.HUD};
    private int activeTab = 0;

    public ArcanicMenu() {
        super(Component.literal("Arcanic"));
        UiSound.play("open");
    }

    public boolean isPauseScreen() { return false; }
    int S(double v) { return (int) Math.round(v * (double) this.u); }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        this.renderBackground(g, mouseX, mouseY, pt);
        this.anim.frame();
        
        int sw = Px.width();
        int sh = Px.height();
        this.u = Math.max(0.45F, Math.min(1.35F, (float) sh / 1080.0F * 1.1F));
        
        this.pw = Math.round(1000 * u);
        this.ph = Math.round(640 * u);
        this.px = (sw - pw) / 2;
        this.py = (sh - ph) / 2;

        this.sideW = Math.round(190 * u);
        this.sideX = this.px;
        this.sideY = this.py;
        this.sideH = this.ph;
        
        this.cx0 = this.sideX + this.sideW + Math.round(24 * u);
        this.cw0 = this.pw - this.sideW - Math.round(48 * u);
        this.top = this.py + Math.round(80 * u);
        this.bottom = this.py + this.ph - Math.round(20 * u);

        Px.begin(g);
        
        // 1. Затемнение фона
        Px.rect(g, 0, 0, sw, sh, 0x99000000);

        // 2. Тень и главная панель
        int R = Math.round(12 * u);
        Px.shadow(g, px, py + S(8), pw, ph, S(24), 0x88000000);
        Px.round(g, px, py, pw, ph, R, 0xFF0A0A0C);
        Px.outline(g, px, py, pw, ph, R, 0x22FFFFFF);

        // 3. Боковая панель (утопленная)
        Px.round(g, sideX, sideY, sideW, sideH, R, 0xFF111114);
        Px.rect(g, sideX + sideW, sideY + S(50), S(1), sideH - S(100), 0x1AFFFFFF);

        // 4. Контентная панель
        Px.round(g, sideX + sideW + S(4), sideY, pw - sideW - S(4), sideH, R, 0xFF0E0E11);
        Px.outline(g, sideX + sideW + S(4), sideY, pw - sideW - S(4), sideH, R, 0x1AFFFFFF);

        // Отрисовка бокового меню (вкладки)
        int is = S(22); // размер иконки лого
        BrandLogo.idle(g, sideX + S(20), sideY + S(20), is);
        Px.text(g, "ARCANIC", sideX + S(50), sideY + S(24), Px.Weight.BOLD, S(16), 0xFFFFFFFF);
        Px.rect(g, sideX + S(20), sideY + S(46), S(30), S(2), Theme.accent());

        int tabY = sideY + S(80);
        String[] tabNames = {"Визуалы", "Утилиты", "HUD"};
        String[] tabIcons = {"eye", "wrench", "layout-dashboard"};
        
        for (int i = 0; i < TABS.length; i++) {
            boolean hv = mouseX >= sideX + S(10) && mouseX <= sideX + sideW - S(10) &&
                         mouseY >= tabY && mouseY <= tabY + S(36);
            boolean on = (activeTab == i);
            
            if (on || hv) {
                Px.round(g, sideX + S(10), tabY, sideW - S(20), S(36), S(8), on ? 0xFF1C1C22 : 0x11FFFFFF);
                if (on) Px.round(g, sideX + S(10), tabY + S(8), S(3), S(20), S(1.5f), Theme.accent());
            }
            
            Px.icon(g, "w." + tabIcons[i], sideX + S(24), tabY + S(9), S(18), on ? Theme.accent() : 0xFF888888);
            Px.text(g, tabNames[i], sideX + S(54), tabY + S(11), Px.Weight.MEDIUM, S(14), on ? 0xFFFFFFFF : 0xFFAAAAAA);
            
            int finalI = i;
            if (hv && !on) {
                // Тут можно добавить клик по вкладке, но пока оставим просто визуал
            }
            tabY += S(44);
        }

        // Шапка контента
        Px.text(g, tabNames[activeTab], cx0, py + S(24), Px.Weight.BOLD, S(22), 0xFFFFFFFF);
        Px.rect(g, cx0, py + S(52), cw0, S(1), 0x22FFFFFF);

        // Отрисовка списка модулей (Упрощенная версия)
        List<Module> list = Modules.of(TABS[activeTab]);
        int listY = top;
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            if (listY > bottom - S(40)) break; // обрезка снизу

            boolean hv = mouseX >= cx0 && mouseX <= cx0 + cw0 && 
                         mouseY >= listY && mouseY <= listY + S(40);

            if (hv) Px.round(g, cx0, listY, cw0, S(40), S(8), 0x11FFFFFF);
            
            Px.text(g, m.title(), cx0 + S(16), listY + S(13), Px.Weight.SEMIBOLD, S(14), 0xFFFFFFFF);
            
            // Рисуем переключатель (визуально)
            int tw = S(36), th = S(20);
            int tx = cx0 + cw0 - tw - S(16);
            int ty = listY + S(10);
            boolean on = m.enabled();
            
            Px.round(g, tx, ty, tw, th, th/2, on ? Theme.accent() : 0xFF333333);
            Px.round(g, on ? tx + tw - th + S(2) : tx + S(2), ty + S(2), th - S(4), th - S(4), (th-S(4))/2, 0xFFFFFFFF);

            listY += S(48);
        }

        Px.end(g);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float pt) {
        if (Minecraft.getInstance().level != null) {
            this.renderBlurredBackground(g);
        } else {
            super.renderBackground(g, mouseX, mouseY, pt);
        }
    }
}
