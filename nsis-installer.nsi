;--------------------------------
;Include Modern UI
	!include "MUI2.nsh"
	!include "nsDialogs.nsh"

;--------------------------------
;General
	Unicode true
  
	!define PRODUCT "SnipSniper"
	!define AFTER_INSTALLATION_URL "https://github.com/CaptureCoop/SnipSniper"

	Name "${PRODUCT}"

	OutFile "release\output\SnipSniper_Win_installer.exe"

	InstallDir "$PROGRAMFILES\${PRODUCT}"

	RequestExecutionLevel admin

	;Show 'console' in installer and uninstaller
	ShowInstDetails "show"
	ShowUninstDetails "show"

	InstallDirRegKey HKLM "Software\${PRODUCT}" ""
	
	!define /file PRODUCT_VERSION "version.txt"
	!define /file VERSION "version.txt"

	;Add .0 at the end because it wants 4 digits
	VIProductVersion "${PRODUCT_VERSION}.0"
	VIFileVersion "${VERSION}.0"
	VIAddVersionKey "FileVersion" "${VERSION}.0"
	VIAddVersionKey "LegalCopyright" "2022 CaptureCoop.org"
	VIAddVersionKey "FileDescription" "SnipSniper Installer"

;--------------------------------
;Interface Settings
	!define MUI_ABORTWARNING
	!define MUI_LANGDLL_ALLLANGUAGES

	!define MUI_ICON "src\main\resources\net\snipsniper\resources\img\icons\snipsniper.ico" # for the Installer
	!define MUI_UNICON "src\main\resources\net\snipsniper\resources\img\icons\snipsniper.ico" # for the later created UnInstaller

	!define MUI_HEADERIMAGE_RIGHT
	!define MUI_WELCOMEFINISHPAGE_BITMAP "nsis-logo.bmp"  # for the Installer
	!define MUI_UNWELCOMEFINISHPAGE_BITMAP "nsis-logo.bmp"  # for the later created UnInstaller

	;No description for components
	!define MUI_COMPONENTSPAGE_NODESC


	var desktop_shortcut
	var autostart_shortcut

	Function finishpageAction
		Pop $desktop_shortcut
		${NSD_GetState} $desktop_shortcut $0
		${If} $0 == 1
			CreateShortcut "$desktop\SnipSniper.lnk" "$INSTDIR\SnipSniper.exe"
			CreateShortcut "$desktop\SnipSniper Editor.lnk" "$INSTDIR\SnipSniper Editor.exe"
			CreateShortcut "$desktop\SnipSniper Viewer.lnk" "$INSTDIR\SnipSniper Viewer.exe"
		${EndIf}
	FunctionEnd

;--------------------------------
;Shortcut page

Function shortcutPage
!insertmacro MUI_HEADER_TEXT "Shortcuts" "Choose what kind of shortcuts to create"

nsDialogs::Create 1018
Pop $0
${If} $0 == error
    Abort
${EndIf}

${NSD_CreateLabel} 0 0 100% 12u "Choose your preferences below"
Pop $0

${NSD_CreateCheckbox} 0 25 100% 15 "Create desktop shortcuts"
Pop $desktop_shortcut

${NSD_CreateCheckbox} 0 50 100% 15 "Add to autostart"
Pop $autostart_shortcut

nsDialogs::Show
FunctionEnd

;--------------------------------
;Pages

	;Installer
	!insertmacro MUI_PAGE_WELCOME # simply remove this and other pages if you don't want it
	!insertmacro MUI_PAGE_LICENSE "LICENSE" # link to an ANSI encoded license file
    !insertmacro MUI_PAGE_DIRECTORY
	Page custom shortcutPage 
	!insertmacro MUI_PAGE_INSTFILES
	!define MUI_PAGE_CUSTOMFUNCTION_PRE finishpageaction
	!define MUI_FINISHPAGE_RUN "$INSTDIR\SnipSniper.exe"
	;!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\readme.txt"
	!insertmacro MUI_PAGE_FINISH

	;Uninstaller
	!insertmacro MUI_UNPAGE_WELCOME
	!insertmacro MUI_UNPAGE_CONFIRM
	!insertmacro MUI_UNPAGE_INSTFILES
	!insertmacro MUI_UNPAGE_FINISH


;--------------------------------
;Languages
	;If language is not found in this list the first one is used
	!insertmacro MUI_LANGUAGE "English"
	!insertmacro MUI_LANGUAGE "French"
	!insertmacro MUI_LANGUAGE "German"
	!insertmacro MUI_LANGUAGE "Spanish"
	!insertmacro MUI_LANGUAGE "SpanishInternational"
	!insertmacro MUI_LANGUAGE "SimpChinese"
	!insertmacro MUI_LANGUAGE "TradChinese"
	!insertmacro MUI_LANGUAGE "Japanese"
	!insertmacro MUI_LANGUAGE "Korean"
	!insertmacro MUI_LANGUAGE "Italian"
	!insertmacro MUI_LANGUAGE "Dutch"
	!insertmacro MUI_LANGUAGE "Danish"
	!insertmacro MUI_LANGUAGE "Swedish"
	!insertmacro MUI_LANGUAGE "Norwegian"
	!insertmacro MUI_LANGUAGE "NorwegianNynorsk"
	!insertmacro MUI_LANGUAGE "Finnish"
	!insertmacro MUI_LANGUAGE "Greek"
	!insertmacro MUI_LANGUAGE "Russian"
	!insertmacro MUI_LANGUAGE "Portuguese"
	!insertmacro MUI_LANGUAGE "PortugueseBR"
	!insertmacro MUI_LANGUAGE "Polish"
	!insertmacro MUI_LANGUAGE "Ukrainian"
	!insertmacro MUI_LANGUAGE "Czech"
	!insertmacro MUI_LANGUAGE "Slovak"
	!insertmacro MUI_LANGUAGE "Croatian"
	!insertmacro MUI_LANGUAGE "Bulgarian"
	!insertmacro MUI_LANGUAGE "Hungarian"
	!insertmacro MUI_LANGUAGE "Thai"
	!insertmacro MUI_LANGUAGE "Romanian"
	!insertmacro MUI_LANGUAGE "Latvian"
	!insertmacro MUI_LANGUAGE "Macedonian"
	!insertmacro MUI_LANGUAGE "Estonian"
	!insertmacro MUI_LANGUAGE "Turkish"
	!insertmacro MUI_LANGUAGE "Lithuanian"
	!insertmacro MUI_LANGUAGE "Slovenian"
	!insertmacro MUI_LANGUAGE "Serbian"
	!insertmacro MUI_LANGUAGE "SerbianLatin"
	!insertmacro MUI_LANGUAGE "Arabic"
	!insertmacro MUI_LANGUAGE "Farsi"
	!insertmacro MUI_LANGUAGE "Hebrew"
	!insertmacro MUI_LANGUAGE "Indonesian"
	!insertmacro MUI_LANGUAGE "Mongolian"
	!insertmacro MUI_LANGUAGE "Luxembourgish"
	!insertmacro MUI_LANGUAGE "Albanian"
	!insertmacro MUI_LANGUAGE "Breton"
	!insertmacro MUI_LANGUAGE "Belarusian"
	!insertmacro MUI_LANGUAGE "Icelandic"
	!insertmacro MUI_LANGUAGE "Malay"
	!insertmacro MUI_LANGUAGE "Bosnian"
	!insertmacro MUI_LANGUAGE "Kurdish"
	!insertmacro MUI_LANGUAGE "Irish"
	!insertmacro MUI_LANGUAGE "Uzbek"
	!insertmacro MUI_LANGUAGE "Galician"
	!insertmacro MUI_LANGUAGE "Afrikaans"
	!insertmacro MUI_LANGUAGE "Catalan"
	!insertmacro MUI_LANGUAGE "Esperanto"
	!insertmacro MUI_LANGUAGE "Asturian"
	!insertmacro MUI_LANGUAGE "Basque"
	!insertmacro MUI_LANGUAGE "Pashto"
	!insertmacro MUI_LANGUAGE "Georgian"
	!insertmacro MUI_LANGUAGE "Vietnamese"
	!insertmacro MUI_LANGUAGE "Welsh"
	!insertmacro MUI_LANGUAGE "Armenian"
	!insertmacro MUI_LANGUAGE "Corsican"

;--------------------------------
;Installer
Section "Main Component"
	SectionIn RO #if in component mode = locked
	SetOutPath $INSTDIR

	File /r "release\raw\*"

	;Store installation folder in registry
	WriteRegStr HKLM "Software\${PRODUCT}" "" $INSTDIR

	;Registry information for add/remove programs
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "DisplayName" "${PRODUCT}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "UninstallString" '"$INSTDIR\${PRODUCT}_uninstall.exe"'
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "NoModify" 1
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "NoRepair" 1

	CreateDirectory "$SMPROGRAMS\${PRODUCT}"
	CreateShortCut "$SMPROGRAMS\${PRODUCT}\SnipSniper.lnk" "$INSTDIR\SnipSniper.exe"
	CreateShortCut "$SMPROGRAMS\${PRODUCT}\SnipSniper Editor.lnk" "$INSTDIR\SnipSniper Editor.exe"
	CreateShortCut "$SMPROGRAMS\${PRODUCT}\SnipSniper Viewer.lnk" "$INSTDIR\SnipSniper Viewer.exe"
	CreateShortCut "$SMPROGRAMS\${PRODUCT}\Uninstall ${PRODUCT}.lnk" "$INSTDIR\${PRODUCT}_uninstaller.exe"

	WriteUninstaller "${PRODUCT}_uninstaller.exe"
SectionEnd

;--------------------------------
;Uninstaller

Section "Uninstall"
	;Remove all registry keys
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}"
	DeleteRegKey HKLM "Software\${PRODUCT}"

	RMDir /r "$INSTDIR\*.*"
	RMDir "$INSTDIR"

	Delete "$SMPROGRAMS\${PRODUCT}\*.*"
	RmDir  "$SMPROGRAMS\${PRODUCT}"

	Delete "$desktop\SnipSniper.lnk"
	Delete "$desktop\SnipSniper Viewer.lnk"
	Delete "$desktop\SnipSniper Editor.lnk" 
SectionEnd

;--------------------------------
;After Installation Function
Function .onInstSuccess
	;ExecShell "open" "microsoft-edge:${AFTER_INSTALLATION_URL}"
FunctionEnd