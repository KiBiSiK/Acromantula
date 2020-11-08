namespace Acromantula_GUI.CodeView
{
    partial class CodeViewMain
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(CodeViewMain));
            this.editorContainer = new System.Windows.Forms.Panel();
            this.searchResults = new ScintillaNET_FindReplaceDialog.FindAllResults.FindAllResultsPanel();
            this.SuspendLayout();
            // 
            // editorContainer
            // 
            this.editorContainer.Anchor = ((System.Windows.Forms.AnchorStyles) ((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) | System.Windows.Forms.AnchorStyles.Left) | System.Windows.Forms.AnchorStyles.Right)));
            this.editorContainer.Location = new System.Drawing.Point(0, 0);
            this.editorContainer.Name = "editorContainer";
            this.editorContainer.Size = new System.Drawing.Size(686, 339);
            this.editorContainer.TabIndex = 0;
            // 
            // searchResults
            // 
            this.searchResults.Anchor = ((System.Windows.Forms.AnchorStyles) (((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left) | System.Windows.Forms.AnchorStyles.Right)));
            this.searchResults.Location = new System.Drawing.Point(0, 345);
            this.searchResults.Name = "searchResults";
            this.searchResults.Scintilla = null;
            this.searchResults.Size = new System.Drawing.Size(686, 151);
            this.searchResults.TabIndex = 1;
            // 
            // CodeViewMain
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(686, 495);
            this.Controls.Add(this.searchResults);
            this.Controls.Add(this.editorContainer);
            this.Icon = ((System.Drawing.Icon) (resources.GetObject("$this.Icon")));
            this.Name = "CodeViewMain";
            this.Text = "CodeView";
            this.Load += new System.EventHandler(this.CodeViewMain_Load);
            this.ResumeLayout(false);
        }

        #endregion

        private System.Windows.Forms.Panel editorContainer;
        private ScintillaNET_FindReplaceDialog.FindAllResults.FindAllResultsPanel searchResults;
    }
}