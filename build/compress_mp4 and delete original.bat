@echo off
for %%T IN (*.mp4) do (
@MultiExtract.exe "%%T" -a
del "%%T"
)